package com.jarvis.launcher.ui.launcher.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.launcher.ui.components.GlowText
import com.jarvis.launcher.ui.launcher.widgets.HudBattery
import com.jarvis.launcher.ui.launcher.widgets.HudClock
import com.jarvis.launcher.ui.launcher.widgets.HudDate
import com.jarvis.launcher.ui.launcher.widgets.HudWeather
import com.jarvis.launcher.ui.theme.HudTextDim
import com.jarvis.launcher.ui.theme.JarvisCyan

/**
 * Main home screen composable for the JARVIS launcher.
 *
 * Layout:
 * - Full-screen [HudBackground] behind everything
 * - Top area: weather (left), battery (right)
 * - Centre-top: clock + date
 * - Bottom: "JARVIS" label with glow + "swipe up" indicator
 *
 * @param modifier Modifier for the root container.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onJarvisActivate: () -> Unit = {},
    onSettingsOpen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    // Swipe-up indicator bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "home_screen")
    val swipeUpOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipe_up_bounce"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Full-screen animated HUD background
        HudBackground()

        // Foreground UI content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(systemBarsPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---- Top row: Weather (left) + Battery (right) ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                HudWeather(
                    modifier = Modifier.padding(top = 8.dp)
                )

                HudBattery(
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ---- Clock ----
            HudClock()

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Date ----
            HudDate()

            // ---- Spacer pushes bottom content down ----
            Spacer(modifier = Modifier.weight(1f))

            // ---- JARVIS label: tap = assistant, long press = settings ----
            GlowText(
                text = "J A R V I S",
                modifier = Modifier.combinedClickable(
                    onClick = { onJarvisActivate() },
                    onLongClick = { onSettingsOpen() }
                ),
                color = JarvisCyan,
                glowColor = JarvisCyan,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 8.sp,
                glowAlpha = 0.4f,
                glowScale = 1.06f
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---- Swipe up indicator ----
            Column(
                modifier = Modifier.graphicsLayer {
                    translationY = swipeUpOffset
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Chevron
                Text(
                    text = "⌃", // ⌃ up-pointing chevron
                    color = HudTextDim,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "SWIPE UP",
                    color = HudTextDim,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
