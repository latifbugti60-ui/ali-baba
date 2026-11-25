package com.example

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.BluePurpleDiagonalGradient
import com.example.ui.theme.BluePurpleHorizontalGradient
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LatifAiStudioTheme
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonSkyBlue
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SubtleCardBorderGradient
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantPurple

object ElevenLabsWebPolicy {
    const val OFFICIAL_URL = "https://elevenlabs.io/"

    private val allowedEmbeddedHosts = setOf(
        "elevenlabs.io",
        "accounts.google.com",
        "appleid.apple.com",
        "github.com",
        "challenges.cloudflare.com",
    )

    private val allowedHostSuffixes = listOf(
        ".elevenlabs.io",
        ".cloudflare.com",
        ".hcaptcha.com",
        ".arkoselabs.com",
        ".recaptcha.net",
        ".gstatic.com",
        ".googleapis.com",
    )

    fun isSafeHttpsUrl(rawUrl: String?): Boolean {
        if (rawUrl.isNullOrBlank()) return false
        val uri = runCatching { Uri.parse(rawUrl) }.getOrNull() ?: return false
        return uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    }

    fun shouldEmbedInWebView(rawUrl: String?): Boolean {
        if (!isSafeHttpsUrl(rawUrl)) return false
        val host = Uri.parse(rawUrl).host?.lowercase() ?: return false
        if (host in allowedEmbeddedHosts) return true
        return allowedHostSuffixes.any { suffix -> host.endsWith(suffix) }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LatifAiStudioTheme {
                LatifAiStudioApp()
            }
        }
    }
}

@Composable
fun LatifAiStudioApp() {
    val context = LocalContext.current
    var showElevenLabsBrowser by rememberSaveable { mutableStateOf(false) }

    val openInExternalBrowser: (String) -> Unit = { targetUrl ->
        val safeUrl = if (ElevenLabsWebPolicy.isSafeHttpsUrl(targetUrl)) {
            targetUrl
        } else {
            ElevenLabsWebPolicy.OFFICIAL_URL
        }
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)))
        }
    }

    AnimatedDarkBackdrop {
        AnimatedContent(
            targetState = showElevenLabsBrowser,
            transitionSpec = {
                fadeIn(tween(240)) togetherWith fadeOut(tween(200))
            },
            label = "home_to_elevenlabs",
        ) { isBrowserOpen ->
            if (!isBrowserOpen) {
                LatifHomeScreen(
                    onOpenElevenLabs = { showElevenLabsBrowser = true },
                )
            } else {
                ElevenLabsBrowserScreen(
                    onCloseToHome = { showElevenLabsBrowser = false },
                    onOpenExternalBrowser = openInExternalBrowser,
                )
            }
        }
    }
}

@Composable
private fun AnimatedDarkBackdrop(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "latif_aurora")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "latif_aurora_drift",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDeep)
            .drawBehind {
                val c1 = Offset(size.width * (0.22f + 0.16f * drift), size.height * 0.25f)
                val c2 = Offset(size.width * (0.78f - 0.16f * drift), size.height * 0.74f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = 0.20f), Color.Transparent),
                        center = c1,
                        radius = size.maxDimension * 0.55f,
                    ),
                    center = c1,
                    radius = size.maxDimension * 0.55f,
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(VibrantPurple.copy(alpha = 0.22f), Color.Transparent),
                        center = c2,
                        radius = size.maxDimension * 0.58f,
                    ),
                    center = c2,
                    radius = size.maxDimension * 0.58f,
                )
            },
    ) {
        content()
    }
}

@Composable
private fun LatifHomeScreen(
    onOpenElevenLabs: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "open_elevenlabs_scale",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "button_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "button_glow_alpha",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        val isWide = maxWidth >= 600.dp

        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_latif_studio_icon_1795613326710),
                contentDescription = stringResource(R.string.cd_brand_logo),
                modifier = Modifier
                    .size(if (isWide) 96.dp else 84.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.5.dp, SubtleCardBorderGradient, RoundedCornerShape(22.dp)),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.brand_headline),
                    style = MaterialTheme.typography.displayLarge.copy(
                        letterSpacing = 1.6.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.brand_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            // ONLY ONE large animated button on the home screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(buttonScale)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                NeonSkyBlue.copy(alpha = glowAlpha),
                                VibrantPurple.copy(alpha = glowAlpha),
                            )
                        ),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .background(BluePurpleHorizontalGradient),
            ) {
                Button(
                    onClick = onOpenElevenLabs,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("open_elevenlabs_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = TextPrimary,
                    ),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.action_open_elevenlabs),
                        style = MaterialTheme.typography.titleLarge.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ElevenLabsBrowserScreen(
    onCloseToHome: () -> Unit,
    onOpenExternalBrowser: (String) -> Unit,
) {
    var loadProgress by remember { mutableIntStateOf(0) }
    var currentUrl by rememberSaveable { mutableStateOf(ElevenLabsWebPolicy.OFFICIAL_URL) }
    var embeddingBlocked by rememberSaveable { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        val webView = webViewRef
        if (webView != null && webView.canGoBack() && !embeddingBlocked) {
            webView.goBack()
        } else {
            onCloseToHome()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.stopLoading()
            webViewRef?.destroy()
            webViewRef = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDeep)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Surface(
            color = ObsidianDeep.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.35f)),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = {
                            val webView = webViewRef
                            if (webView != null && webView.canGoBack() && !embeddingBlocked) {
                                webView.goBack()
                            } else {
                                onCloseToHome()
                            }
                        },
                        modifier = Modifier.testTag("close_browser_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_close_webview),
                            tint = TextPrimary,
                        )
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MintEmerald,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = Uri.parse(currentUrl).host ?: "elevenlabs.io",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                embeddingBlocked = false
                                webViewRef?.reload()
                            },
                            modifier = Modifier.testTag("reload_browser_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.cd_webview_reload),
                                tint = TextSecondary,
                            )
                        }
                        IconButton(
                            onClick = { onOpenExternalBrowser(currentUrl) },
                            modifier = Modifier.testTag("open_external_browser_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = stringResource(R.string.cd_open_external_browser),
                                tint = NeonSkyBlue,
                            )
                        }
                    }
                }

                if (loadProgress in 1..99 && !embeddingBlocked) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = NeonSkyBlue,
                        trackColor = ObsidianElevated,
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (embeddingBlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier.widthIn(max = 500.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = GlassCardFill,
                        border = BorderStroke(1.dp, SubtleCardBorderGradient),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = NeonSkyBlue,
                                modifier = Modifier.size(36.dp),
                            )
                            Text(
                                text = stringResource(R.string.embedding_blocked_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = stringResource(R.string.embedding_blocked_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                            )
                            Button(
                                onClick = { onOpenExternalBrowser(currentUrl) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BluePurpleDiagonalGradient)
                                    .testTag("fallback_open_browser_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = TextPrimary,
                                ),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.action_open_in_browser),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                }
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("elevenlabs_webview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            val webViewInstance = this
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                            setBackgroundColor(android.graphics.Color.parseColor("#060914"))

                            CookieManager.getInstance().apply {
                                setAcceptCookie(true)
                                setAcceptThirdPartyCookies(webViewInstance, true)
                            }

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = false
                                displayZoomControls = false
                                allowFileAccess = false
                                allowContentAccess = false
                                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    loadProgress = newProgress
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    if (url != null && ElevenLabsWebPolicy.isSafeHttpsUrl(url)) {
                                        currentUrl = url
                                    }
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                ): Boolean {
                                    val target = request?.url?.toString() ?: return true
                                    if (!ElevenLabsWebPolicy.isSafeHttpsUrl(target)) {
                                        return true
                                    }
                                    return if (ElevenLabsWebPolicy.shouldEmbedInWebView(target)) {
                                        false
                                    } else {
                                        onOpenExternalBrowser(target)
                                        true
                                    }
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?,
                                ) {
                                    if (request?.isForMainFrame == true) {
                                        embeddingBlocked = true
                                    }
                                }

                                override fun onReceivedHttpError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    errorResponse: WebResourceResponse?,
                                ) {
                                    val code = errorResponse?.statusCode ?: return
                                    if (request?.isForMainFrame == true && (code == 403 || code >= 500)) {
                                        embeddingBlocked = true
                                    }
                                }
                            }

                            setDownloadListener { downloadUrl, _, _, _, _ ->
                                if (ElevenLabsWebPolicy.isSafeHttpsUrl(downloadUrl)) {
                                    onOpenExternalBrowser(downloadUrl)
                                } else {
                                    onOpenExternalBrowser(currentUrl)
                                }
                            }

                            loadUrl(ElevenLabsWebPolicy.OFFICIAL_URL)
                            webViewRef = this
                        }
                    },
                )
            }
        }
    }
}
