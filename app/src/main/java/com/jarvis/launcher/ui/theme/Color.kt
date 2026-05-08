package com.jarvis.launcher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val JarvisCyan = Color(0xFF00E5FF)
val JarvisCyanDark = Color(0xFF0097A7)
val JarvisCyanGlow = Color(0x4D00E5FF)
val JarvisBlue = Color(0xFF2196F3)
val JarvisBlueDark = Color(0xFF0D47A1)
val JarvisAmber = Color(0xFFFFAB00)
val HudBackground = Color(0xFF000000)
val HudSurface = Color(0xB30A1929)
val HudSurfaceBright = Color(0x330A1929)
val HudBorder = Color(0x8000E5FF)
val HudText = Color(0xFFB2EBF2)
val HudTextDim = Color(0x99B2EBF2)

data class HudColors(
    val accent: Color = JarvisCyan,
    val accentDark: Color = JarvisCyanDark,
    val accentGlow: Color = JarvisCyanGlow,
    val background: Color = HudBackground,
    val surface: Color = HudSurface,
    val border: Color = accent.copy(alpha = 0.5f),
    val text: Color = HudText,
    val textDim: Color = HudTextDim
)

val LocalHudColors = compositionLocalOf { HudColors() }
