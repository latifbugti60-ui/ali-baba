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
    private const val BASE_URL = "https://api.mail.tm"
    private val secureRandom = SecureRandom()
    private val aliasChars = "abcdefghijklmnopqrstuvwxyz0123456789"
    private val passChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$"

    suspend fun createMailbox(): Result<TempMailbox> = withContext(Dispatchers.IO) {
        runCatching {
            val domainsJson = JSONObject(httpGet("$BASE_URL/domains", token = null))
            val members = domainsJson.optJSONArray("hydra:member") ?: JSONArray()
            var activeDomain: String? = null
            for (i in 0 until members.length()) {
                val obj = members.optJSONObject(i) ?: continue
                if (obj.optBoolean("isActive", true)) {
                    activeDomain = obj.optString("domain").takeIf { it.isNotBlank() }
                    if (activeDomain != null) break
                }
            }
            val domain = activeDomain ?: error("No active temporary email domain available right now.")
            val alias = "latif" + randomString(aliasChars, 7)
            val address = "$alias@$domain"
            val ephemeralPassword = randomString(passChars, 18)

            val accountPayload = JSONObject().apply {
                put("address", address)
                put("password", ephemeralPassword)
            }
            httpPost("$BASE_URL/accounts", accountPayload.toString())

            val tokenPayload = JSONObject().apply {
                put("address", address)
                put("password", ephemeralPassword)
            }
            val tokenResponse = JSONObject(httpPost("$BASE_URL/token", tokenPayload.toString()))
            val token = tokenResponse.optString("token")
            require(token.isNotBlank()) { "Failed to obtain temporary inbox token." }

            TempMailbox(address = address, token = token)
        }
    }

    suspend fun fetchMessages(token: String): Result<List<TempMailMessage>> = withContext(Dispatchers.IO) {
        runCatching {
            val json = JSONObject(httpGet("$BASE_URL/messages", token = token))
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

    suspend fun fetchMessageBody(token: String, messageId: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val safeId = messageId.replace(Regex("[^A-Za-z0-9_-]"), "")
            require(safeId.isNotBlank()) { "Invalid message id." }
            val json = JSONObject(httpGet("$BASE_URL/messages/$safeId", token = token))
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
