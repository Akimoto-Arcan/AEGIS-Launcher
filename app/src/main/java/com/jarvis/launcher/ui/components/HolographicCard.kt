package com.jarvis.launcher.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.launcher.ui.theme.HudSurface
import com.jarvis.launcher.ui.theme.JarvisCyan

/**
 * A translucent card with HUD-style corner brackets and a continuously
 * moving horizontal scan line, evoking a holographic display panel.
 *
 * @param modifier Modifier for the outer container.
 * @param backgroundColor The translucent card background.
 * @param scanLineColor Color of the scan line highlight.
 * @param scanLineDuration Duration in milliseconds for a full top-to-bottom sweep.
 * @param scanLineHeight Height of the scan line gradient band in dp.
 * @param cornerLength Length of the HUD corner bracket arms.
 * @param contentPadding Padding applied inside the card around the content.
 * @param content Composable content displayed inside the card.
 */
@Composable
fun HolographicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = HudSurface,
    scanLineColor: Color = JarvisCyan.copy(alpha = 0.10f),
    scanLineDuration: Int = 3000,
    scanLineHeight: Dp = 40.dp,
    cornerLength: Dp = 20.dp,
    contentPadding: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "holographic_scan")

    val scanProgress by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = scanLineDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line_y"
    )

    Box(
        modifier = modifier
            .hudBorder(cornerLength = cornerLength)
            .background(backgroundColor)
            .drawWithContent {
                drawContent()

                // Draw the scan line overlay
                val slHeight = scanLineHeight.toPx()
                val yCenter = size.height * scanProgress
                val yTop = yCenter - slHeight / 2f
                val yBottom = yCenter + slHeight / 2f

                // Clamp to drawable area
                val drawTop = yTop.coerceAtLeast(0f)
                val drawBottom = yBottom.coerceAtMost(size.height)

                if (drawBottom > drawTop) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                scanLineColor,
                                scanLineColor.copy(alpha = scanLineColor.alpha * 1.5f),
                                scanLineColor,
                                Color.Transparent
                            ),
                            startY = drawTop,
                            endY = drawBottom
                        ),
                        topLeft = Offset(0f, drawTop),
                        size = Size(size.width, drawBottom - drawTop)
                    )
                }
            }
            .padding(contentPadding),
        content = content
    )
}
