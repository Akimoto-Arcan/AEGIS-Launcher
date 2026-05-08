package com.jarvis.launcher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.jarvis.launcher.ui.theme.LocalHudColors

@Composable
fun AppLaunchAnimation(
    isPlaying: Boolean,
    originX: Float,
    originY: Float,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = LocalHudColors.current.accent
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(500)
            )
            onFinished()
        }
    }

    if (isPlaying && progress.value > 0f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val center = Offset(originX, originY)
            val maxRadius = size.maxDimension
            val currentRadius = maxRadius * progress.value

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.4f * (1f - progress.value)),
                        accent.copy(alpha = 0.1f * (1f - progress.value)),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius
                ),
                center = center,
                radius = currentRadius
            )

            drawCircle(
                color = accent.copy(alpha = 0.6f * (1f - progress.value)),
                center = center,
                radius = currentRadius * 0.3f
            )
        }
    }
}
