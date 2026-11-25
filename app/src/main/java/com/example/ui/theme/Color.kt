package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val ObsidianDeep = Color(0xFF060914)
val ObsidianSurface = Color(0xFF0D1428)
val ObsidianElevated = Color(0xFF131D38)
val GlassCardFill = Color(0xCC111A33)
val GlassCardSoft = Color(0x990C1326)
val GlassBorder = Color(0x4D60A5FA)
val PurpleBorder = Color(0x55A78BFA)

val ElectricBlue = Color(0xFF3B82F6)
val NeonSkyBlue = Color(0xFF38BDF8)
val VibrantPurple = Color(0xFF8B5CF6)
val SoftLavender = Color(0xFFC4B5FD)
val MintEmerald = Color(0xFF10B981)
val WarmAmber = Color(0xFFF59E0B)
val RoseCoral = Color(0xFFF43F5E)

val TextPrimary = Color(0xFFF4F7FF)
val TextSecondary = Color(0xFFA3B4D4)
val TextMuted = Color(0xFF6B7FA6)

val BluePurpleHorizontalGradient = Brush.horizontalGradient(
    colors = listOf(ElectricBlue, VibrantPurple)
)

val BluePurpleDiagonalGradient = Brush.linearGradient(
    colors = listOf(NeonSkyBlue, ElectricBlue, VibrantPurple)
)

val SubtleCardBorderGradient = Brush.linearGradient(
    colors = listOf(
        ElectricBlue.copy(alpha = 0.45f),
        VibrantPurple.copy(alpha = 0.45f),
    )
)
