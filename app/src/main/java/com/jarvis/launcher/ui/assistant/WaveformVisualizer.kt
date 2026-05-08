package com.jarvis.launcher.ui.assistant

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.jarvis.launcher.ui.theme.LocalHudColors
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = LocalHudColors.current.accent
    val accentDark = LocalHudColors.current.accentDark

    val transition = rememberInfiniteTransition(label = "waveform")

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val amplitude by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "amplitude"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val barCount = 40
        val barWidth = size.width / (barCount * 2f)
        val centerY = size.height / 2
        val maxBarHeight = size.height * 0.4f

        for (i in 0 until barCount) {
            val x = (i.toFloat() / barCount) * size.width
            val wave1 = sin(x * 0.05f + phase) * maxBarHeight
            val wave2 = sin(x * 0.08f + phase * 1.3f) * maxBarHeight * 0.6f
            val combinedHeight = if (isActive) {
                (wave1 + wave2) * amplitude
            } else {
                (wave1 + wave2) * 0.15f
            }

            val barAlpha = if (isActive) 0.5f + (amplitude * 0.5f) else 0.2f

            drawLine(
                color = if (i % 3 == 0) accent.copy(alpha = barAlpha)
                else accentDark.copy(alpha = barAlpha * 0.7f),
                start = Offset(x, centerY - combinedHeight),
                end = Offset(x, centerY + combinedHeight),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }

        drawLine(
            color = accent.copy(alpha = 0.3f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1.dp.toPx()
        )
    }
}
