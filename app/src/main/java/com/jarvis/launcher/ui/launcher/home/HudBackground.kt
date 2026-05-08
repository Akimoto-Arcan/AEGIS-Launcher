package com.jarvis.launcher.ui.launcher.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jarvis.launcher.ui.theme.HudBackground as HudBackgroundColor

/**
 * Composite full-screen HUD background that layers:
 * 1. Solid black background
 * 2. ScanningLines (very subtle grid + sweeping line)
 * 3. RotatingRings (concentric dashed rings)
 * 4. ArcReactorAnimation (centred reactor core)
 *
 * Additional content can be placed on top via [content].
 *
 * @param modifier Modifier for the root Box.
 * @param content Composable lambda for overlaying UI on top of the background.
 */
@Composable
fun HudBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Layer 1: Subtle scanning grid and sweep
        ScanningLines(
            gridLineAlpha = 0.03f,
            scanLineAlpha = 0.08f,
            scanDurationMillis = 5000
        )

        // Layer 2: Rotating dashed rings
        RotatingRings()

        // Layer 3: Central arc reactor
        ArcReactorAnimation()

        // Layer 4: Caller-supplied content on top of everything
        content()
    }
}
