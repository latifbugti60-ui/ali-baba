package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.BluePurpleDiagonalGradient
import com.example.ui.theme.BluePurpleHorizontalGradient
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.GlassCardSoft
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LatifAiStudioTheme
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonSkyBlue
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SubtleCardBorderGradient
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantPurple
import com.example.ui.theme.WarmAmber
import kotlinx.coroutines.launch

enum class NordVpnServer(
    val countryName: String,
    val regionCode: String,
    val locationNote: String,
) {
    IRELAND("Ireland", "IE", "Dublin • EU West"),
    SWEDEN("Sweden", "SE", "Stockholm • EU North"),
}

object OfficialAccessPolicy {
    const val ELEVENLABS_URL = "https://elevenlabs.io/"
    const val NORDVPN_PACKAGE = "com.nordvpn.android"
    const val NORDVPN_PLAY_URL = "https://play.google.com/store/apps/details?id=com.nordvpn.android"
    const val NORDVPN_ACCOUNT_URL = "https://my.nordaccount.com/"

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

    fun isNordVpnInstalled(context: Context): Boolean {
        return runCatching {
            context.packageManager.getLaunchIntentForPackage(NORDVPN_PACKAGE) != null
        }.getOrDefault(false)
    }

    fun isSystemVpnActive(context: Context): Boolean {
        return runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        }.getOrDefault(false)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LatifAiStudioTheme {
                LatifVoiceAccessApp()
            }
        }
    }
}

@Composable
fun LatifVoiceAccessApp() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showElevenLabsBrowser by rememberSaveable { mutableStateOf(false) }
    var selectedServer by rememberSaveable { mutableStateOf(NordVpnServer.IRELAND) }
    var nordVpnInstalled by remember { mutableStateOf(OfficialAccessPolicy.isNordVpnInstalled(context)) }
    var systemVpnActive by remember { mutableStateOf(OfficialAccessPolicy.isSystemVpnActive(context)) }

    // Refresh real system VPN and installed package status whenever user returns to the app.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                nordVpnInstalled = OfficialAccessPolicy.isNordVpnInstalled(context)
                systemVpnActive = OfficialAccessPolicy.isSystemVpnActive(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val openInExternalBrowser: (String) -> Unit = { targetUrl ->
        val safeUrl = if (OfficialAccessPolicy.isSafeHttpsUrl(targetUrl)) {
            targetUrl
        } else {
            OfficialAccessPolicy.ELEVENLABS_URL
        }
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)))
        }.onFailure {
            scope.launch {
                snackbarHostState.showSnackbar("Unable to open browser for $safeUrl")
            }
        }
    }

    val launchOfficialNordVpn: () -> Unit = {
        clipboardManager.setText(AnnotatedString(selectedServer.countryName))
        val launchIntent = context.packageManager.getLaunchIntentForPackage(OfficialAccessPolicy.NORDVPN_PACKAGE)
        if (launchIntent != null) {
            runCatching {
                context.startActivity(launchIntent)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Copied \"${selectedServer.countryName}\" & opening official NordVPN app",
                        duration = SnackbarDuration.Short,
                    )
                }
            }.onFailure {
                openInExternalBrowser(OfficialAccessPolicy.NORDVPN_PLAY_URL)
            }
        } else {
            openInExternalBrowser(OfficialAccessPolicy.NORDVPN_PLAY_URL)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "NordVPN app not found — opening official Google Play page (${selectedServer.countryName} copied)",
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    val openSystemVpnSettings: () -> Unit = {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
        }.onFailure {
            scope.launch {
                snackbarHostState.showSnackbar("Open Android Settings → Network & Internet → VPN.")
            }
        }
    }

    AnimatedDarkBackdrop {
        AnimatedContent(
            targetState = showElevenLabsBrowser,
            transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(200)) },
            label = "home_to_elevenlabs",
        ) { isBrowserOpen ->
            if (!isBrowserOpen) {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                containerColor = ObsidianElevated,
                                contentColor = TextPrimary,
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(
                                    text = data.visuals.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    },
                ) { innerPadding ->
                    LatifVoiceAccessHome(
                        selectedServer = selectedServer,
                        nordVpnInstalled = nordVpnInstalled,
                        systemVpnActive = systemVpnActive,
                        onSelectServer = { server ->
                            selectedServer = server
                            clipboardManager.setText(AnnotatedString(server.countryName))
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Selected ${server.countryName} (${server.regionCode})",
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        },
                        onConnectNordVpn = launchOfficialNordVpn,
                        onOpenNordVpnAccount = { openInExternalBrowser(OfficialAccessPolicy.NORDVPN_ACCOUNT_URL) },
                        onOpenVpnSettings = openSystemVpnSettings,
                        onOpenElevenLabs = { showElevenLabsBrowser = true },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
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
                val c1 = Offset(size.width * (0.20f + 0.16f * drift), size.height * 0.22f)
                val c2 = Offset(size.width * (0.80f - 0.16f * drift), size.height * 0.78f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = 0.22f), Color.Transparent),
                        center = c1,
                        radius = size.maxDimension * 0.56f,
                    ),
                    center = c1,
                    radius = size.maxDimension * 0.56f,
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
private fun LatifVoiceAccessHome(
    selectedServer: NordVpnServer,
    nordVpnInstalled: Boolean,
    systemVpnActive: Boolean,
    onSelectServer: (NordVpnServer) -> Unit,
    onConnectNordVpn: () -> Unit,
    onOpenNordVpnAccount: () -> Unit,
    onOpenVpnSettings: () -> Unit,
    onOpenElevenLabs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        val isWide = maxWidth >= 720.dp

        Column(
            modifier = Modifier
                .widthIn(max = 1040.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Minimal Brand Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_latif_voice_access_1795615537570),
                    contentDescription = stringResource(R.string.cd_brand_logo),
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, SubtleCardBorderGradient, RoundedCornerShape(20.dp)),
                )
                Text(
                    text = stringResource(R.string.brand_headline),
                    style = MaterialTheme.typography.displayLarge.copy(
                        letterSpacing = 1.4.sp,
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

            // ONLY TWO MAIN FEATURES: 1. NordVPN, 2. ElevenLabs
            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    NordVpnFeatureCard(
                        selectedServer = selectedServer,
                        nordVpnInstalled = nordVpnInstalled,
                        systemVpnActive = systemVpnActive,
                        onSelectServer = onSelectServer,
                        onConnectNordVpn = onConnectNordVpn,
                        onOpenNordVpnAccount = onOpenNordVpnAccount,
                        onOpenVpnSettings = onOpenVpnSettings,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    ElevenLabsFeatureCard(
                        onOpenElevenLabs = onOpenElevenLabs,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            } else {
                NordVpnFeatureCard(
                    selectedServer = selectedServer,
                    nordVpnInstalled = nordVpnInstalled,
                    systemVpnActive = systemVpnActive,
                    onSelectServer = onSelectServer,
                    onConnectNordVpn = onConnectNordVpn,
                    onOpenNordVpnAccount = onOpenNordVpnAccount,
                    onOpenVpnSettings = onOpenVpnSettings,
                    modifier = Modifier.fillMaxWidth(),
                )
                ElevenLabsFeatureCard(
                    onOpenElevenLabs = onOpenElevenLabs,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun NordVpnFeatureCard(
    selectedServer: NordVpnServer,
    nordVpnInstalled: Boolean,
    systemVpnActive: Boolean,
    onSelectServer: (NordVpnServer) -> Unit,
    onConnectNordVpn: () -> Unit,
    onOpenNordVpnAccount: () -> Unit,
    onOpenVpnSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = GlassCardFill,
        border = BorderStroke(1.dp, SubtleCardBorderGradient),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.18f))
                            .border(1.dp, ElectricBlue.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = NeonSkyBlue,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.nordvpn_card_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                        )
                        Text(
                            text = if (nordVpnInstalled) "Official App Installed" else "Official App / Portal",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftLavender,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (systemVpnActive) MintEmerald.copy(alpha = 0.16f) else ObsidianElevated,
                    border = BorderStroke(
                        1.dp,
                        if (systemVpnActive) MintEmerald.copy(alpha = 0.6f) else GlassBorder.copy(alpha = 0.4f),
                    ),
                ) {
                    Text(
                        text = if (systemVpnActive) "System VPN Active" else "VPN Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (systemVpnActive) MintEmerald else TextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.nordvpn_server_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    NordVpnServer.entries.forEach { server ->
                        val isSelected = selectedServer == server
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectServer(server) }
                                .testTag("server_chip_${server.countryName.lowercase()}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) {
                                ElectricBlue.copy(alpha = 0.24f)
                            } else {
                                ObsidianDeep.copy(alpha = 0.7f)
                            },
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) NeonSkyBlue else GlassBorder.copy(alpha = 0.35f),
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(
                                        text = server.countryName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) NeonSkyBlue else TextPrimary,
                                    )
                                    Text(
                                        text = server.locationNote,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                    )
                                }
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Public,
                                    contentDescription = null,
                                    tint = if (isSelected) NeonSkyBlue else TextSecondary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }

            AnimatedGradientButton(
                label = "${stringResource(R.string.action_connect_nordvpn)} (${selectedServer.countryName})",
                icon = Icons.Default.VpnKey,
                onClick = onConnectNordVpn,
                testTag = "connect_nordvpn_button",
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onOpenNordVpnAccount,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("nordvpn_account_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftLavender),
                ) {
                    Text(
                        text = stringResource(R.string.action_open_nordvpn_account),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                OutlinedButton(
                    onClick = onOpenVpnSettings,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("android_vpn_settings_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                ) {
                    Text(
                        text = "VPN Settings",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WarmAmber.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, WarmAmber.copy(alpha = 0.35f)),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WarmAmber,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.nordvpn_limitation_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ElevenLabsFeatureCard(
    onOpenElevenLabs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = GlassCardFill,
        border = BorderStroke(1.dp, SubtleCardBorderGradient),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(VibrantPurple.copy(alpha = 0.20f))
                        .border(1.dp, VibrantPurple.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = SoftLavender,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.elevenlabs_card_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Official Website • Direct Voice Generation",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonSkyBlue,
                    )
                }
            }

            Text(
                text = stringResource(R.string.elevenlabs_card_body),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GlassCardSoft,
                border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.35f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MintEmerald,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = OfficialAccessPolicy.ELEVENLABS_URL,
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonSkyBlue,
                    )
                }
            }

            AnimatedGradientButton(
                label = stringResource(R.string.action_open_elevenlabs),
                icon = Icons.Default.GraphicEq,
                onClick = onOpenElevenLabs,
                testTag = "open_elevenlabs_button",
            )
        }
    }
}

@Composable
private fun AnimatedGradientButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn_scale",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "btn_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "btn_glow_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        NeonSkyBlue.copy(alpha = glowAlpha),
                        VibrantPurple.copy(alpha = glowAlpha),
                    )
                ),
                shape = RoundedCornerShape(16.dp),
            )
            .background(BluePurpleHorizontalGradient),
    ) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .minimumInteractiveComponentSize()
                .testTag(testTag),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = TextPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
    var currentUrl by rememberSaveable { mutableStateOf(OfficialAccessPolicy.ELEVENLABS_URL) }
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
                                    if (url != null && OfficialAccessPolicy.isSafeHttpsUrl(url)) {
                                        currentUrl = url
                                    }
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                ): Boolean {
                                    val target = request?.url?.toString() ?: return true
                                    if (!OfficialAccessPolicy.isSafeHttpsUrl(target)) {
                                        return true
                                    }
                                    return if (OfficialAccessPolicy.shouldEmbedInWebView(target)) {
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
                                if (OfficialAccessPolicy.isSafeHttpsUrl(downloadUrl)) {
                                    onOpenExternalBrowser(downloadUrl)
                                } else {
                                    onOpenExternalBrowser(currentUrl)
                                }
                            }

                            loadUrl(OfficialAccessPolicy.ELEVENLABS_URL)
                            webViewRef = this
                        }
                    },
                )
            }
        }
    }
}
