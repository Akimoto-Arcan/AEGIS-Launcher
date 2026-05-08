package com.jarvis.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = HudBackground,
    primaryContainer = JarvisCyanDark,
    secondary = JarvisBlue,
    onSecondary = HudBackground,
    secondaryContainer = JarvisBlueDark,
    background = HudBackground,
    onBackground = HudText,
    surface = HudSurface,
    onSurface = HudText,
    surfaceVariant = HudSurfaceBright,
    onSurfaceVariant = HudTextDim,
    outline = HudBorder
)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = JarvisTypography,
        content = content
    )
}
