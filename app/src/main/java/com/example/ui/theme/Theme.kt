package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LatifStudioColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextPrimary,
    primaryContainer = ObsidianElevated,
    onPrimaryContainer = SoftLavender,
    secondary = VibrantPurple,
    onSecondary = TextPrimary,
    tertiary = NeonSkyBlue,
    onTertiary = ObsidianDeep,
    error = RoseCoral,
    onError = TextPrimary,
    background = ObsidianDeep,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
)

private val LatifStudioShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun LatifAiStudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LatifStudioColorScheme,
        typography = SmartGmailTypography,
        shapes = LatifStudioShapes,
        content = content,
    )
}
