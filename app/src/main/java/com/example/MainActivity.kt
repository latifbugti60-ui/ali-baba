package com.example

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
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
import androidx.compose.runtime.movableContentOf
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

enum class EmbeddedBrowserMode {
    IDLE,
    ELEVENLABS,
    NORD_ACCOUNT,
}

object OfficialAccessPolicy {
    const val ELEVENLABS_URL = "https://elevenlabs.io/"
    const val NORDVPN_PACKAGE = "com.nordvpn.android"
    const val NORDVPN_ACCOUNT_URL = "https://my.nordaccount.com/"

    private val allowedEmbeddedHosts = setOf(
        "elevenlabs.io",
        "my.nordaccount.com",
        "nordaccount.com",
        "nordvpn.com",
        "accounts.google.com",
        "appleid.apple.com",
        "github.com",
        "login.microsoftonline.com",
        "checkout.stripe.com",
        "challenges.cloudflare.com",
    )

    private val allowedHostSuffixes = listOf(
        ".elevenlabs.io",
        ".nordaccount.com",
        ".nordvpn.com",
        ".stripe.com",
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
                LatifVpnControlApp()
            }
        }
    }
}

@Composable
fun LatifVpnControlApp() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedServer by rememberSaveable { mutableStateOf(NordVpnServer.IRELAND) }
    var browserMode by rememberSaveable { mutableStateOf(EmbeddedBrowserMode.IDLE) }
    var targetBrowserUrl by rememberSaveable { mutableStateOf(OfficialAccessPolicy.ELEVENLABS_URL) }
    var isFullScreenBrowser by rememberSaveable { mutableStateOf(false) }

    var nordVpnInstalled by remember { mutableStateOf(OfficialAccessPolicy.isNordVpnInstalled(context)) }
    var systemVpnActive by remember { mutableStateOf(OfficialAccessPolicy.isSystemVpnActive(context)) }

    // Observe real Android VPN transport status live + refresh on resume.
    DisposableEffect(context, lifecycleOwner) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val vpnRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                systemVpnActive = OfficialAccessPolicy.isSystemVpnActive(context)
            }

            override fun onLost(network: Network) {
                systemVpnActive = OfficialAccessPolicy.isSystemVpnActive(context)
            }
        }
        runCatching { cm?.registerNetworkCallback(vpnRequest, networkCallback) }

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                nordVpnInstalled = OfficialAccessPolicy.isNordVpnInstalled(context)
                systemVpnActive = OfficialAccessPolicy.isSystemVpnActive(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            runCatching { cm?.unregisterNetworkCallback(networkCallback) }
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    val connectServerThroughNordVpn: (NordVpnServer) -> Unit = { server ->
        selectedServer = server
        clipboardManager.setText(AnnotatedString(server.countryName))
        val launchIntent = context.packageManager.getLaunchIntentForPackage(OfficialAccessPolicy.NORDVPN_PACKAGE)
        if (launchIntent != null) {
            runCatching {
                context.startActivity(launchIntent)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Copied \"${server.countryName}\" & launched official NordVPN app",
                        duration = SnackbarDuration.Short,
                    )
                }
            }.onFailure {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Unable to launch NordVPN app. Sign in via NordVPN Account inside the panel.",
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Official NordVPN app is not installed on this device. Use NordVPN Account inside the panel or install NordVPN.",
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }

    val disconnectNordVpn: () -> Unit = {
        if (!systemVpnActive) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "VPN status is already Disconnected.",
                    duration = SnackbarDuration.Short,
                )
            }
        } else {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(OfficialAccessPolicy.NORDVPN_PACKAGE)
            val openedOfficialApp = if (launchIntent != null) {
                runCatching { context.startActivity(launchIntent) }.isSuccess
            } else {
                false
            }
            if (!openedOfficialApp) {
                runCatching { context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS)) }
                    .onFailure {
                        scope.launch {
                            snackbarHostState.showSnackbar("Open Android Settings → Network & Internet → VPN to disconnect.")
                        }
                    }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Opened official NordVPN app so you can disconnect securely.",
                        duration = SnackbarDuration.Short,
                    )
                }
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

    val openInEmbeddedPanel: (EmbeddedBrowserMode, String) -> Unit = { mode, url ->
        browserMode = mode
        targetBrowserUrl = url
    }

    val showInAppNotice: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    val embeddedPanelContent = remember {
        movableContentOf { panelModifier: Modifier ->
            ElevenLabsEmbeddedPanel(
                browserMode = browserMode,
                targetUrl = targetBrowserUrl,
                isFullScreen = isFullScreenBrowser,
                onOpenElevenLabs = {
                    openInEmbeddedPanel(EmbeddedBrowserMode.ELEVENLABS, OfficialAccessPolicy.ELEVENLABS_URL)
                },
                onSwitchToElevenLabs = {
                    openInEmbeddedPanel(EmbeddedBrowserMode.ELEVENLABS, OfficialAccessPolicy.ELEVENLABS_URL)
                },
                onToggleFullScreen = {
                    isFullScreenBrowser = !isFullScreenBrowser
                },
                onShowNotice = showInAppNotice,
                modifier = panelModifier,
            )
        }
    }

    AnimatedDarkBackdrop {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(
                        horizontal = if (isFullScreenBrowser) 8.dp else 16.dp,
                        vertical = if (isFullScreenBrowser) 8.dp else 14.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                AnimatedVisibility(
                    visible = !isFullScreenBrowser,
                    enter = fadeIn(tween(220)),
                    exit = fadeOut(tween(160)),
                ) {
                    DashboardHeader(systemVpnActive = systemVpnActive)
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    val isWideDashboard = maxWidth >= 680.dp

                    if (isFullScreenBrowser) {
                        embeddedPanelContent(Modifier.fillMaxSize())
                    } else if (isWideDashboard) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            NordVpnControlPanel(
                                selectedServer = selectedServer,
                                nordVpnInstalled = nordVpnInstalled,
                                systemVpnActive = systemVpnActive,
                                onSelectAndConnectServer = connectServerThroughNordVpn,
                                onOpenNordAccount = {
                                    openInEmbeddedPanel(
                                        EmbeddedBrowserMode.NORD_ACCOUNT,
                                        OfficialAccessPolicy.NORDVPN_ACCOUNT_URL,
                                    )
                                },
                                onDisconnect = disconnectNordVpn,
                                onOpenVpnSettings = openSystemVpnSettings,
                                modifier = Modifier
                                    .weight(0.39f)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState()),
                            )
                            embeddedPanelContent(
                                Modifier
                                    .weight(0.61f)
                                    .fillMaxHeight(),
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            NordVpnControlPanel(
                                selectedServer = selectedServer,
                                nordVpnInstalled = nordVpnInstalled,
                                systemVpnActive = systemVpnActive,
                                onSelectAndConnectServer = connectServerThroughNordVpn,
                                onOpenNordAccount = {
                                    openInEmbeddedPanel(
                                        EmbeddedBrowserMode.NORD_ACCOUNT,
                                        OfficialAccessPolicy.NORDVPN_ACCOUNT_URL,
                                    )
                                },
                                onDisconnect = disconnectNordVpn,
                                onOpenVpnSettings = openSystemVpnSettings,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            embeddedPanelContent(
                                Modifier
                                    .fillMaxWidth()
                                    .height(520.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(systemVpnActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_latif_voice_access_1795615537570),
                contentDescription = stringResource(R.string.cd_brand_logo),
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.2.dp, SubtleCardBorderGradient, RoundedCornerShape(14.dp)),
            )
            Column {
                Text(
                    text = stringResource(R.string.brand_headline),
                    style = MaterialTheme.typography.titleLarge.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.brand_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        VpnStatusBadge(systemVpnActive = systemVpnActive)
    }
}

@Composable
private fun VpnStatusBadge(systemVpnActive: Boolean) {
    val badgeColor = if (systemVpnActive) MintEmerald else WarmAmber
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = badgeColor.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.55f)),
        modifier = Modifier.testTag("vpn_status_badge"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
            )
            Text(
                text = stringResource(
                    if (systemVpnActive) R.string.status_connected else R.string.status_disconnected
                ),
                style = MaterialTheme.typography.labelLarge,
                color = badgeColor,
            )
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
private fun NordVpnControlPanel(
    selectedServer: NordVpnServer,
    nordVpnInstalled: Boolean,
    systemVpnActive: Boolean,
    onSelectAndConnectServer: (NordVpnServer) -> Unit,
    onOpenNordAccount: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenVpnSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        color = GlassCardFill,
        border = BorderStroke(1.dp, SubtleCardBorderGradient),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.18f))
                            .border(1.dp, ElectricBlue.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = NeonSkyBlue,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.nordvpn_card_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                        Text(
                            text = if (nordVpnInstalled) "Official App Ready" else "App Not Detected",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftLavender,
                        )
                    }
                }
            }

            // NordVPN Account connection (opens inside embedded panel, never a new browser tab)
            AnimatedGradientButton(
                label = stringResource(R.string.action_nordvpn_account),
                icon = Icons.Default.VpnKey,
                onClick = onOpenNordAccount,
                testTag = "nordvpn_account_button",
            )

            // Server buttons: clicking either Ireland or Sweden connects through official NordVPN app
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.nordvpn_server_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                )

                NordVpnServer.entries.forEach { server ->
                    val isSelected = selectedServer == server
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectAndConnectServer(server) }
                            .testTag("server_chip_${server.countryName.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            ElectricBlue.copy(alpha = 0.24f)
                        } else {
                            ObsidianDeep.copy(alpha = 0.72f)
                        },
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) NeonSkyBlue else GlassBorder.copy(alpha = 0.35f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
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
                                    text = "${server.locationNote} • Tap to Connect",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                )
                            }
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Public,
                                contentDescription = null,
                                tint = if (isSelected) NeonSkyBlue else TextSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onDisconnect,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("disconnect_nordvpn_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WarmAmber.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmAmber),
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.action_disconnect_vpn),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
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
                        text = stringResource(R.string.action_vpn_settings),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ElevenLabsEmbeddedPanel(
    browserMode: EmbeddedBrowserMode,
    targetUrl: String,
    isFullScreen: Boolean,
    onOpenElevenLabs: () -> Unit,
    onSwitchToElevenLabs: () -> Unit,
    onToggleFullScreen: () -> Unit,
    onShowNotice: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var loadProgress by remember { mutableIntStateOf(0) }
    var currentUrl by rememberSaveable { mutableStateOf(OfficialAccessPolicy.ELEVENLABS_URL) }
    var embeddingBlocked by rememberSaveable { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = isFullScreen || (browserMode != EmbeddedBrowserMode.IDLE && webViewRef?.canGoBack() == true)) {
        val webView = webViewRef
        if (webView != null && webView.canGoBack() && !embeddingBlocked) {
            webView.goBack()
        } else if (isFullScreen) {
            onToggleFullScreen()
        }
    }

    LaunchedEffect(browserMode, targetUrl) {
        if (browserMode != EmbeddedBrowserMode.IDLE && OfficialAccessPolicy.isSafeHttpsUrl(targetUrl)) {
            embeddingBlocked = false
            currentUrl = targetUrl
            webViewRef?.let { webView ->
                if (webView.url != targetUrl) {
                    webView.loadUrl(targetUrl)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.stopLoading()
            webViewRef?.destroy()
            webViewRef = null
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = GlassCardFill,
        border = BorderStroke(1.dp, SubtleCardBorderGradient),
    ) {
        if (browserMode == EmbeddedBrowserMode.IDLE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(VibrantPurple.copy(alpha = 0.22f))
                            .border(1.dp, VibrantPurple.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = SoftLavender,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.elevenlabs_idle_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.elevenlabs_card_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GlassCardSoft,
                        border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.35f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = ObsidianDeep.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.35f)),
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val webView = webViewRef
                                        if (webView != null && webView.canGoBack() && !embeddingBlocked) {
                                            webView.goBack()
                                        }
                                    },
                                    modifier = Modifier.testTag("webview_back_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.cd_webview_back),
                                        tint = TextPrimary,
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        embeddingBlocked = false
                                        webViewRef?.reload()
                                    },
                                    modifier = Modifier.testTag("webview_reload_button"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = stringResource(R.string.cd_webview_reload),
                                        tint = TextSecondary,
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp),
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

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (browserMode == EmbeddedBrowserMode.NORD_ACCOUNT) {
                                    OutlinedButton(
                                        onClick = onSwitchToElevenLabs,
                                        modifier = Modifier
                                            .height(36.dp)
                                            .testTag("switch_to_elevenlabs_button"),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, NeonSkyBlue.copy(alpha = 0.6f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonSkyBlue),
                                    ) {
                                        Text(
                                            text = "ElevenLabs",
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onToggleFullScreen,
                                    modifier = Modifier.testTag("fullscreen_toggle_button"),
                                ) {
                                    Icon(
                                        imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = stringResource(R.string.cd_fullscreen_toggle),
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
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
                                    onClick = {
                                        embeddingBlocked = false
                                        webViewRef?.loadUrl(targetUrl)
                                    },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BluePurpleDiagonalGradient)
                                        .testTag("reload_embedded_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = TextPrimary,
                                    ),
                                ) {
                                    Text(
                                        text = stringResource(R.string.action_reload),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
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
                                                onShowNotice("Blocked outside link to keep everything inside the app.")
                                                true
                                            }
                                        }

                                        override fun onReceivedError(
                                            view: WebView?,
                                            request: WebResourceRequest?,
                                            error: WebResourceError?,
                                        ) {
                                            if (request?.isForMainFrame == true) {
                                                view?.loadUrl("about:blank")
                                                embeddingBlocked = true
                                            }
                                        }

                                        override fun onReceivedHttpError(
                                            view: WebView?,
                                            request: WebResourceRequest?,
                                            errorResponse: WebResourceResponse?,
                                        ) {
                                            val code = errorResponse?.statusCode ?: return
                                            if (request?.isForMainFrame == true && (code == 403 || code == 429 || code >= 500)) {
                                                view?.loadUrl("about:blank")
                                                embeddingBlocked = true
                                            }
                                        }
                                    }

                                    // Keep downloads inside Android DownloadManager without opening external browser tabs.
                                    setDownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, _ ->
                                        if (OfficialAccessPolicy.isSafeHttpsUrl(downloadUrl)) {
                                            runCatching {
                                                val fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType)
                                                val dmRequest = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                                                    setMimeType(mimeType)
                                                    val cookies = CookieManager.getInstance().getCookie(downloadUrl)
                                                    if (!cookies.isNullOrBlank()) {
                                                        addRequestHeader("Cookie", cookies)
                                                    }
                                                    addRequestHeader("User-Agent", userAgent)
                                                    setTitle(fileName)
                                                    setNotificationVisibility(
                                                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                                                    )
                                                    setDestinationInExternalPublicDir(
                                                        Environment.DIRECTORY_DOWNLOADS,
                                                        fileName,
                                                    )
                                                }
                                                val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                                dm.enqueue(dmRequest)
                                                onShowNotice("Downloading $fileName to Downloads...")
                                            }.onFailure {
                                                onShowNotice("Download failed inside embedded browser.")
                                            }
                                        } else {
                                            onShowNotice("Use ElevenLabs audio player controls inside the embedded view.")
                                        }
                                    }

                                    loadUrl(targetUrl)
                                    webViewRef = this
                                }
                            },
                        )
                    }
                }
            }
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
                .height(54.dp)
                .minimumInteractiveComponentSize()
                .testTag(testTag),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = TextPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
