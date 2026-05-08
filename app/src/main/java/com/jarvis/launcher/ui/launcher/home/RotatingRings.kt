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
import com.jarvis.launcher.ui.theme.JarvisCyan
import com.jarvis.launcher.ui.theme.JarvisCyanDark
import com.jarvis.launcher.ui.theme.JarvisBlue

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
    val rings = remember {
        listOf(
            RingConfig(
                radiusFraction = 0.48f,
                strokeWidth = 1.2f,
                dashLength = 30f,
                gapLength = 20f,
                durationMillis = 25000,
                clockwise = true,
                color = JarvisCyan,
                alpha = 0.3f
            ),
            RingConfig(
                radiusFraction = 0.42f,
                strokeWidth = 0.8f,
                dashLength = 15f,
                gapLength = 25f,
                durationMillis = 18000,
                clockwise = false,
                color = JarvisCyanDark,
                alpha = 0.25f
            ),
            RingConfig(
                radiusFraction = 0.36f,
                strokeWidth = 1.5f,
                dashLength = 40f,
                gapLength = 15f,
                durationMillis = 30000,
                clockwise = true,
                color = JarvisBlue,
                alpha = 0.2f
            ),
            RingConfig(
                radiusFraction = 0.30f,
                strokeWidth = 0.6f,
                dashLength = 8f,
                gapLength = 30f,
                durationMillis = 14000,
                clockwise = false,
                color = JarvisCyan,
                alpha = 0.2f
            ),
            RingConfig(
                radiusFraction = 0.55f,
                strokeWidth = 0.5f,
                dashLength = 50f,
                gapLength = 10f,
                durationMillis = 35000,
                clockwise = true,
                color = JarvisCyanDark,
                alpha = 0.15f
            )
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
