package com.jarvis.launcher.ui.launcher.widgets

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.launcher.ui.theme.HudTextDim
import com.jarvis.launcher.ui.theme.JarvisCyan

/**
 * Placeholder weather display styled to match the HUD theme.
 *
 * Shows "WEATHER MODULE" with a subtle scan-line sweep and
 * a temperature placeholder. Ready to be connected to a real
 * weather data source.
 *
 * @param modifier Modifier for the outer Column.
 * @param temperature Temperature string to display (default placeholder).
 * @param condition Weather condition string (default placeholder).
 */
@Composable
fun HudWeather(
    modifier: Modifier = Modifier,
    temperature: String = "-- °C",
    condition: String = "AWAITING DATA"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_scan")

    val scanProgress by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weather_scan_y"
    )

    Column(
        modifier = modifier
            .drawWithContent {
                drawContent()

                // Subtle scan line overlay
                val lineY = size.height * scanProgress
                if (lineY in 0f..size.height) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                JarvisCyan.copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            startY = (lineY - 8f).coerceAtLeast(0f),
                            endY = (lineY + 8f).coerceAtMost(size.height)
                        ),
                        topLeft = Offset(0f, (lineY - 8f).coerceAtLeast(0f)),
                        size = Size(
                            size.width,
                            16f.coerceAtMost(size.height - (lineY - 8f).coerceAtLeast(0f))
                        )
                    )
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WEATHER MODULE",
            color = HudTextDim,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = temperature,
            color = JarvisCyan,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Light,
            fontSize = 22.sp,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = condition,
            color = HudTextDim,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp
        )
    }
}
