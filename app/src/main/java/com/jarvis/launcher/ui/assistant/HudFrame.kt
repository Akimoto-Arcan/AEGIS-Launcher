package com.jarvis.launcher.ui.assistant

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.jarvis.launcher.ui.theme.LocalHudColors

@Composable
fun HudFrame(modifier: Modifier = Modifier) {
    val accent = LocalHudColors.current.accent
    val accentDark = LocalHudColors.current.accentDark

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 2.dp.toPx()
        val cornerLen = 30.dp.toPx()
        val pad = 8.dp.toPx()

        val topLeft = Offset(pad, pad)
        val topRight = Offset(size.width - pad, pad)
        val bottomLeft = Offset(pad, size.height - pad)
        val bottomRight = Offset(size.width - pad, size.height - pad)

        // Top-left corner
        drawLine(accent, topLeft, Offset(topLeft.x + cornerLen, topLeft.y), strokeWidth, StrokeCap.Square)
        drawLine(accent, topLeft, Offset(topLeft.x, topLeft.y + cornerLen), strokeWidth, StrokeCap.Square)

        // Top-right corner
        drawLine(accent, topRight, Offset(topRight.x - cornerLen, topRight.y), strokeWidth, StrokeCap.Square)
        drawLine(accent, topRight, Offset(topRight.x, topRight.y + cornerLen), strokeWidth, StrokeCap.Square)

        // Bottom-left corner
        drawLine(accent, bottomLeft, Offset(bottomLeft.x + cornerLen, bottomLeft.y), strokeWidth, StrokeCap.Square)
        drawLine(accent, bottomLeft, Offset(bottomLeft.x, bottomLeft.y - cornerLen), strokeWidth, StrokeCap.Square)

        // Bottom-right corner
        drawLine(accent, bottomRight, Offset(bottomRight.x - cornerLen, bottomRight.y), strokeWidth, StrokeCap.Square)
        drawLine(accent, bottomRight, Offset(bottomRight.x, bottomRight.y - cornerLen), strokeWidth, StrokeCap.Square)

        // Subtle connecting lines (top and bottom edges)
        drawLine(
            accentDark.copy(alpha = 0.3f),
            Offset(topLeft.x + cornerLen, topLeft.y),
            Offset(topRight.x - cornerLen, topRight.y),
            1.dp.toPx()
        )
        drawLine(
            accentDark.copy(alpha = 0.3f),
            Offset(bottomLeft.x + cornerLen, bottomLeft.y),
            Offset(bottomRight.x - cornerLen, bottomRight.y),
            1.dp.toPx()
        )
    }
}
