package com.example.tempmail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import javax.net.ssl.HttpsURLConnection

data class TempMailbox(
    val address: String,
    val token: String,
    val baseUrl: String,
    val webUrl: String,
    val providerName: String,
)

data class TempMailMessage(
    val id: String,
    val fromAddress: String,
    val fromName: String,
    val subject: String,
    val intro: String,
    val createdAt: String,
    val bodyText: String? = null,
)

object TempMailRepository {
    private data class ProviderSpec(val name: String, val apiBase: String, val webUrl: String)

    private val providers = listOf(
        ProviderSpec("Mail.tm", "https://api.mail.tm", "https://mail.tm/"),
        ProviderSpec("Mail.gw", "https://api.mail.gw", "https://mail.gw/"),
    )

    private val secureRandom = SecureRandom()
    private val aliasChars = "abcdefghijklmnopqrstuvwxyz0123456789"
    private val passChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$"

    suspend fun createMailbox(): Result<TempMailbox> = withContext(Dispatchers.IO) {
        var lastError: Throwable? = null
        for (provider in providers) {
            val attempt = runCatching {
                createOnProvider(provider.name, provider.apiBase, provider.webUrl)
            }
            if (attempt.isSuccess) {
                return@withContext attempt
            }
            lastError = attempt.exceptionOrNull()
        }
        Result.failure(lastError ?: IllegalStateException("Temporary email providers are currently busy. Try again in a few seconds."))
    }

    private fun createOnProvider(providerName: String, apiBase: String, webUrl: String): TempMailbox {
        val domainsJson = JSONObject(httpGet("$apiBase/domains", token = null))
        val members = domainsJson.optJSONArray("hydra:member") ?: JSONArray()
        val activeDomains = buildList {
            for (i in 0 until members.length()) {
                val obj = members.optJSONObject(i) ?: continue
                if (obj.optBoolean("isActive", true)) {
                    val domain = obj.optString("domain")
                    if (domain.isNotBlank()) add(domain)
                }
            }
        }
        require(activeDomains.isNotEmpty()) { "No active temporary email domain available right now." }
        val domain = activeDomains[secureRandom.nextInt(activeDomains.size)]
        val alias = "user" + randomString(aliasChars, 8)
        val address = "$alias@$domain"
        val ephemeralPassword = randomString(passChars, 18)

        val accountPayload = JSONObject().apply {
            put("address", address)
            put("password", ephemeralPassword)
        }
        httpPost("$apiBase/accounts", accountPayload.toString())

        val tokenPayload = JSONObject().apply {
            put("address", address)
            put("password", ephemeralPassword)
        }
        val tokenResponse = JSONObject(httpPost("$apiBase/token", tokenPayload.toString()))
        val token = tokenResponse.optString("token")
        require(token.isNotBlank()) { "Failed to obtain temporary inbox token." }

        return TempMailbox(
            address = address,
            token = token,
            baseUrl = apiBase,
            webUrl = webUrl,
            providerName = providerName,
        )
    }

    suspend fun fetchMessages(mailbox: TempMailbox): Result<List<TempMailMessage>> = withContext(Dispatchers.IO) {
        runCatching {
            val json = JSONObject(httpGet("${mailbox.baseUrl}/messages", token = mailbox.token))
            val members = json.optJSONArray("hydra:member") ?: JSONArray()
            buildList {
                for (i in 0 until members.length()) {
                    val item = members.optJSONObject(i) ?: continue
                    val fromObj = item.optJSONObject("from")
                    add(
                        TempMailMessage(
                            id = item.optString("id"),
                            fromAddress = fromObj?.optString("address").orEmpty(),
                            fromName = fromObj?.optString("name").orEmpty(),
                            subject = item.optString("subject").ifBlank { "(No Subject)" },
                            intro = item.optString("intro"),
                            createdAt = item.optString("createdAt"),
                        )
                    )
                }
            }
        }
    }

    suspend fun fetchMessageBody(mailbox: TempMailbox, messageId: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val safeId = messageId.replace(Regex("[^A-Za-z0-9_-]"), "")
            require(safeId.isNotBlank()) { "Invalid message id." }
            val json = JSONObject(httpGet("${mailbox.baseUrl}/messages/$safeId", token = mailbox.token))
            val plain = json.optString("text")
            if (plain.isNotBlank()) {
                plain
            } else {
                val htmlArray = json.optJSONArray("html")
                val rawHtml = htmlArray?.optString(0).orEmpty()
                stripHtmlTags(rawHtml).ifBlank { "(Empty message body)" }
            }
        }
    }

    private fun stripHtmlTags(html: String): String {
        return html
            .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun randomString(alphabet: String, length: Int): String {
        return buildString(length) {
            repeat(length) {
                append(alphabet[secureRandom.nextInt(alphabet.length)])
            }
        }
    }

    private fun httpGet(urlStr: String, token: String?): String {
        val conn = (URL(urlStr).openConnection() as HttpsURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "LatifVpnControl/1.0 (Android)")
            if (!token.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
        }
        return readResponse(conn)
    }

    private fun httpPost(urlStr: String, jsonBody: String): String {
        val conn = (URL(urlStr).openConnection() as HttpsURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "LatifVpnControl/1.0 (Android)")
        }
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(jsonBody)
        }
        return readResponse(conn)
    }

    private fun readResponse(conn: HttpURLConnection): String {
        return try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
            if (code !in 200..299) {
                val detail = runCatching { JSONObject(body).optString("detail") }.getOrNull()
                error(detail?.takeIf { it.isNotBlank() } ?: "Temp Mail request failed (HTTP $code)")
            }
            body
        } finally {
            conn.disconnect()
        }
    }
}
