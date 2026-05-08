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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.jarvis.launcher.ui.theme.JarvisCyan
import com.jarvis.launcher.ui.theme.JarvisCyanDark

/**
 * Draws subtle scanning effects:
 * 1. A faint background grid of horizontal lines
 * 2. A bright horizontal scan line that sweeps top-to-bottom,
 *    leaving a fading trail behind it
 *
 * @param modifier Modifier for the canvas.
 * @param gridLineAlpha Alpha for the static grid lines.
 * @param gridSpacingDp Approximate spacing between grid lines in px.
 * @param scanLineAlpha Peak alpha of the sweeping scan line.
 * @param scanDurationMillis Duration for a full top-to-bottom sweep.
 * @param trailHeight Height of the fading trail behind the scan line, as a fraction of canvas height.
 */
@Composable
fun ScanningLines(
    modifier: Modifier = Modifier,
    gridLineAlpha: Float = 0.04f,
    gridSpacingDp: Float = 32f,
    scanLineAlpha: Float = 0.15f,
    scanDurationMillis: Int = 4000,
    trailHeight: Float = 0.15f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning_lines")

    val scanProgress by infiniteTransition.animateFloat(
        initialValue = -0.05f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = scanDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_y_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // ---- Static grid lines ----
        val gridColor = JarvisCyanDark.copy(alpha = gridLineAlpha)
        val spacing = gridSpacingDp
        var y = 0f
        while (y < height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 0.5f
            )
            y += spacing
        }

        // Vertical grid lines (sparser)
        var x = 0f
        val verticalSpacing = spacing * 2f
        while (x < width) {
            drawLine(
                color = gridColor.copy(alpha = gridLineAlpha * 0.5f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 0.5f
            )
            x += verticalSpacing
        }

        // ---- Sweeping scan line with fading trail ----
        val scanY = height * scanProgress
        val trailPx = height * trailHeight

        // Trail gradient (fades from transparent to scan line color)
        val trailTop = (scanY - trailPx).coerceAtLeast(0f)
        val trailBottom = scanY.coerceAtMost(height)

        if (trailBottom > trailTop) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        JarvisCyan.copy(alpha = scanLineAlpha * 0.3f),
                        JarvisCyan.copy(alpha = scanLineAlpha * 0.7f)
                    ),
                    startY = trailTop,
                    endY = trailBottom
                ),
                topLeft = Offset(0f, trailTop),
                size = Size(width, trailBottom - trailTop)
            )
        }

        // Bright scan line
        if (scanY in 0f..height) {
            drawLine(
                color = JarvisCyan.copy(alpha = scanLineAlpha),
                start = Offset(0f, scanY),
                end = Offset(width, scanY),
                strokeWidth = 1.5f
            )
            // Glow around the line
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        JarvisCyan.copy(alpha = scanLineAlpha * 0.4f),
                        Color.Transparent
                    ),
                    startY = (scanY - 4f).coerceAtLeast(0f),
                    endY = (scanY + 4f).coerceAtMost(height)
                ),
                topLeft = Offset(0f, (scanY - 4f).coerceAtLeast(0f)),
                size = Size(width, 8f.coerceAtMost(height - (scanY - 4f).coerceAtLeast(0f)))
            )
        }
    }
}
