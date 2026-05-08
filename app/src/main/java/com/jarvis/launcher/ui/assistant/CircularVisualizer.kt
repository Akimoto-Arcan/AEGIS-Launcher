package com.jarvis.launcher.ui.assistant

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.jarvis.launcher.ui.theme.LocalHudColors

@Composable
fun CircularVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val hudColors = LocalHudColors.current
    val accent = hudColors.accent
    val accentDark = hudColors.accentDark
    val accentGlow = hudColors.accentGlow

    val transition = rememberInfiniteTransition(label = "visualizer")

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val ripple by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple"
    )

    Canvas(modifier = modifier.size(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2

        if (isActive) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentGlow, Color.Transparent),
                    center = center,
                    radius = maxRadius * ripple
                ),
                center = center,
                radius = maxRadius * ripple,
                alpha = 1f - ripple
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = 0.3f * pulse),
                    Color.Transparent
                ),
                center = center,
                radius = maxRadius * 0.8f
            ),
            center = center,
            radius = maxRadius * 0.8f
        )

        drawArc(
            color = accent.copy(alpha = if (isActive) pulse else 0.4f),
            startAngle = rotation,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(center.x - maxRadius * 0.7f, center.y - maxRadius * 0.7f),
            size = androidx.compose.ui.geometry.Size(maxRadius * 1.4f, maxRadius * 1.4f)
        )

        drawArc(
            color = accentDark.copy(alpha = if (isActive) pulse else 0.3f),
            startAngle = -rotation * 0.7f,
            sweepAngle = 120f,
            useCenter = false,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(center.x - maxRadius * 0.55f, center.y - maxRadius * 0.55f),
            size = androidx.compose.ui.geometry.Size(maxRadius * 1.1f, maxRadius * 1.1f)
        )

        drawArc(
            color = accent.copy(alpha = if (isActive) 0.8f * pulse else 0.3f),
            startAngle = rotation * 1.5f,
            sweepAngle = 60f,
            useCenter = false,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(center.x - maxRadius * 0.4f, center.y - maxRadius * 0.4f),
            size = androidx.compose.ui.geometry.Size(maxRadius * 0.8f, maxRadius * 0.8f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, accent, accentDark),
                center = center,
                radius = maxRadius * 0.15f
            ),
            center = center,
            radius = maxRadius * 0.15f * if (isActive) pulse else 0.8f
        )
    }
}
