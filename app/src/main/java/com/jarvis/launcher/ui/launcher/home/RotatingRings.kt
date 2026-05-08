package com.jarvis.launcher.ui.launcher.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.jarvis.launcher.ui.theme.LocalHudColors

/**
 * Configuration for a single rotating ring.
 *
 * @param radiusFraction Radius as a fraction of the minimum canvas dimension / 2.
 * @param strokeWidth Stroke width in pixels.
 * @param dashLength Length of each dash segment.
 * @param gapLength Length of each gap between dashes.
 * @param durationMillis Duration for a full 360-degree rotation.
 * @param clockwise True for clockwise, false for counter-clockwise.
 * @param color Ring color.
 * @param alpha Ring alpha.
 */
data class RingConfig(
    val radiusFraction: Float,
    val strokeWidth: Float,
    val dashLength: Float,
    val gapLength: Float,
    val durationMillis: Int,
    val clockwise: Boolean,
    val color: Color,
    val alpha: Float = 1f
)

/**
 * Draws 5 concentric dashed rings rotating at different speeds and directions.
 *
 * Each ring is configured via [RingConfig] and animated with an infinite transition.
 */
@Composable
fun RotatingRings(
    modifier: Modifier = Modifier
) {
    val hudColors = LocalHudColors.current
    val accent = hudColors.accent
    val accentDark = hudColors.accentDark

    val rings = remember(accent, accentDark) {
        listOf(
            RingConfig(0.48f, 1.2f, 30f, 20f, 25000, true, accent, 0.3f),
            RingConfig(0.42f, 0.8f, 15f, 25f, 18000, false, accentDark, 0.25f),
            RingConfig(0.36f, 1.5f, 40f, 15f, 30000, true, accentDark, 0.2f),
            RingConfig(0.30f, 0.6f, 8f, 30f, 14000, false, accent, 0.2f),
            RingConfig(0.55f, 0.5f, 50f, 10f, 35000, true, accentDark, 0.15f)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rotating_rings")

    // Create rotation animations for each ring
    val rotations = rings.mapIndexed { index, ring ->
        val start = if (ring.clockwise) 0f else 360f
        val end = if (ring.clockwise) 360f else 0f
        infiniteTransition.animateFloat(
            initialValue = start,
            targetValue = end,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = ring.durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ring_rotation_$index"
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = size.minDimension / 2f

        rings.forEachIndexed { index, ring ->
            val rotation by rotations[index]
            val radius = maxRadius * ring.radiusFraction
            val diameter = radius * 2f
            val topLeft = Offset(centerX - radius, centerY - radius)

            rotate(degrees = rotation, pivot = Offset(centerX, centerY)) {
                drawArc(
                    color = ring.color.copy(alpha = ring.alpha),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(
                        width = ring.strokeWidth,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(ring.dashLength, ring.gapLength),
                            phase = 0f
                        )
                    )
                )
            }
        }
    }
}
