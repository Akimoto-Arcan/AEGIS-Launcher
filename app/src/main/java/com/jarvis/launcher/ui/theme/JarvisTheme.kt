package com.jarvis.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun JarvisTheme(
    hudColors: HudColors = HudColors(),
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = hudColors.accent,
        onPrimary = HudBackground,
        primaryContainer = hudColors.accentDark,
        secondary = hudColors.accentDark,
        onSecondary = HudBackground,
        background = HudBackground,
        onBackground = HudText,
        surface = HudSurface,
        onSurface = HudText,
        surfaceVariant = HudSurfaceBright,
        onSurfaceVariant = HudTextDim,
        outline = hudColors.border
    )

    CompositionLocalProvider(LocalHudColors provides hudColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = JarvisTypography,
            content = content
        )
    }
}
