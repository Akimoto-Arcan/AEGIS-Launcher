package com.jarvis.launcher.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.launcher.ui.theme.HudBorder
import com.jarvis.launcher.ui.theme.JarvisCyan

/**
 * Draws L-shaped HUD corner brackets at each corner of the composable.
 *
 * @param color The color of the corner brackets.
 * @param cornerLength The length of each arm of the L-shaped bracket.
 * @param strokeWidth The stroke width of the bracket lines.
 * @param glowRadius The blur radius for the glow effect behind the brackets.
 * @param inset Padding inward from the composable edges.
 */
fun Modifier.hudBorder(
    color: Color = HudBorder,
    glowColor: Color = JarvisCyan,
    cornerLength: Dp = 20.dp,
    strokeWidth: Dp = 1.5.dp,
    glowRadius: Dp = 4.dp,
    inset: Dp = 0.dp
): Modifier = this.drawBehind {
    val cLen = cornerLength.toPx()
    val sw = strokeWidth.toPx()
    val ins = inset.toPx()
    val glow = glowRadius.toPx()

    val left = ins
    val top = ins
    val right = size.width - ins
    val bottom = size.height - ins

    // Glow pass
    drawIntoCanvas { canvas ->
        val glowPaint = Paint().apply {
            this.color = glowColor
            this.style = PaintingStyle.Stroke
            this.strokeWidth = sw * 2f
            this.isAntiAlias = true
            asFrameworkPaint().apply {
                maskFilter = android.graphics.BlurMaskFilter(
                    glow,
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
        }
        drawCornerBrackets(canvas, glowPaint, left, top, right, bottom, cLen)
    }

    // Crisp foreground pass
    drawIntoCanvas { canvas ->
        val fgPaint = Paint().apply {
            this.color = color
            this.style = PaintingStyle.Stroke
            this.strokeWidth = sw
            this.isAntiAlias = true
        }
        drawCornerBrackets(canvas, fgPaint, left, top, right, bottom, cLen)
    }
}

private fun drawCornerBrackets(
    canvas: androidx.compose.ui.graphics.Canvas,
    paint: Paint,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    cornerLength: Float
) {
    val path = androidx.compose.ui.graphics.Path()

    // Top-left corner
    path.moveTo(left, top + cornerLength)
    path.lineTo(left, top)
    path.lineTo(left + cornerLength, top)

    // Top-right corner
    path.moveTo(right - cornerLength, top)
    path.lineTo(right, top)
    path.lineTo(right, top + cornerLength)

    // Bottom-right corner
    path.moveTo(right, bottom - cornerLength)
    path.lineTo(right, bottom)
    path.lineTo(right - cornerLength, bottom)

    // Bottom-left corner
    path.moveTo(left + cornerLength, bottom)
    path.lineTo(left, bottom)
    path.lineTo(left, bottom - cornerLength)

    canvas.drawPath(path, paint)
}

/**
 * Convenience overload that draws full HUD borders with default Jarvis styling.
 */
fun Modifier.hudBorderAccent(
    cornerLength: Dp = 24.dp,
    strokeWidth: Dp = 2.dp,
    glowRadius: Dp = 6.dp
): Modifier = hudBorder(
    color = JarvisCyan.copy(alpha = 0.9f),
    glowColor = JarvisCyan,
    cornerLength = cornerLength,
    strokeWidth = strokeWidth,
    glowRadius = glowRadius
)
