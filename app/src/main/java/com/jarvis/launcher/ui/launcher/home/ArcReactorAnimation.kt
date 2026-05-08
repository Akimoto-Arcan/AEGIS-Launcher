package com.jarvis.launcher.ui.launcher.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.launcher.ui.theme.LocalHudColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The centrepiece arc reactor animation, drawn entirely with Canvas.
 *
 * Features:
 * - Central bright white/cyan circle with radial gradient
 * - Outer glow halo that pulses in and out
 * - 3 concentric segmented arcs rotating at different speeds via dash path effects
 * - Inner hexagonal detail pattern
 *
 * All animations are driven by [rememberInfiniteTransition].
 *
 * @param modifier Modifier (should include a size or use the default 220.dp).
 * @param size Intrinsic size of the reactor canvas.
 */
@Composable
fun ArcReactorAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val hudColors = LocalHudColors.current
    val accentColor = hudColors.accent
    val accentDark = hudColors.accentDark

    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")

    // Outer glow pulse (scale factor)
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Ring rotations (degrees)
    val ring1Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1_rotation"
    )

    val ring2Rotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2_rotation"
    )

    val ring3Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3_rotation"
    )

    // Inner hexagon slow rotation
    val hexRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hex_rotation"
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val maxRadius = this.size.minDimension / 2f

        // ---- Outer glow halo ----
        val glowRadius = maxRadius * glowPulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.08f),
                    accentColor.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = center,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = center
        )

        // ---- Segmented arc ring 1 (outermost) ----
        drawSegmentedRing(
            rotation = ring1Rotation,
            radiusFraction = 0.92f,
            maxRadius = maxRadius,
            strokeWidth = 2.5f,
            dashOn = 18f,
            dashOff = 12f,
            color = accentColor.copy(alpha = 0.7f)
        )

        // ---- Segmented arc ring 2 (middle) ----
        drawSegmentedRing(
            rotation = ring2Rotation,
            radiusFraction = 0.75f,
            maxRadius = maxRadius,
            strokeWidth = 3f,
            dashOn = 24f,
            dashOff = 8f,
            color = accentColor.copy(alpha = 0.55f)
        )

        // ---- Segmented arc ring 3 (inner) ----
        drawSegmentedRing(
            rotation = ring3Rotation,
            radiusFraction = 0.58f,
            maxRadius = maxRadius,
            strokeWidth = 2f,
            dashOn = 10f,
            dashOff = 16f,
            color = accentDark.copy(alpha = 0.6f)
        )

        // ---- Inner hexagon detail ----
        rotate(degrees = hexRotation, pivot = center) {
            drawHexagon(center, maxRadius * 0.38f, accentColor.copy(alpha = 0.35f), strokeWidth = 1.5f)
        }

        // Second hexagon, opposite rotation
        rotate(degrees = -hexRotation * 0.5f, pivot = center) {
            drawHexagon(center, maxRadius * 0.28f, accentDark.copy(alpha = 0.25f), strokeWidth = 1f)
        }

        // ---- Inner triangle ----
        rotate(degrees = hexRotation * 0.7f, pivot = center) {
            drawTriangle(center, maxRadius * 0.20f, accentColor.copy(alpha = 0.3f), strokeWidth = 1f)
        }

        // ---- Central bright core ----
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    accentColor.copy(alpha = 0.9f),
                    accentColor.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                center = center,
                radius = maxRadius * 0.18f
            ),
            radius = maxRadius * 0.18f,
            center = center
        )

        // Bright inner dot
        drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = maxRadius * 0.05f,
            center = center
        )
    }
}

/**
 * Draws a segmented (dashed) arc ring around the center.
 */
private fun DrawScope.drawSegmentedRing(
    rotation: Float,
    radiusFraction: Float,
    maxRadius: Float,
    strokeWidth: Float,
    dashOn: Float,
    dashOff: Float,
    color: Color
) {
    val radius = maxRadius * radiusFraction
    val diameter = radius * 2f
    val topLeft = Offset(center.x - radius, center.y - radius)

    rotate(degrees = rotation, pivot = center) {
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(dashOn, dashOff),
                    phase = 0f
                )
            )
        )
    }
}

/**
 * Draws a regular hexagon outline.
 */
private fun DrawScope.drawHexagon(
    center: Offset,
    radius: Float,
    color: Color,
    strokeWidth: Float
) {
    val path = androidx.compose.ui.graphics.Path().apply {
        for (i in 0 until 6) {
            val angle = (PI / 3.0 * i - PI / 6.0).toFloat()
            val x = center.x + radius * cos(angle)
            val y = center.y + radius * sin(angle)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

/**
 * Draws an equilateral triangle outline.
 */
private fun DrawScope.drawTriangle(
    center: Offset,
    radius: Float,
    color: Color,
    strokeWidth: Float
) {
    val path = androidx.compose.ui.graphics.Path().apply {
        for (i in 0 until 3) {
            val angle = (2.0 * PI / 3.0 * i - PI / 2.0).toFloat()
            val x = center.x + radius * cos(angle)
            val y = center.y + radius * sin(angle)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}
