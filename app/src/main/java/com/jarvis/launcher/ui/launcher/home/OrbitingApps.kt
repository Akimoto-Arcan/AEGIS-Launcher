package com.jarvis.launcher.ui.launcher.home

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.jarvis.launcher.ui.theme.LocalHudColors
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun OrbitingApps(
    favoritePackages: List<String>,
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (favoritePackages.isEmpty()) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val hudColors = LocalHudColors.current

    var boxWidth by remember { mutableFloatStateOf(0f) }
    var boxHeight by remember { mutableFloatStateOf(0f) }
    var currentAngle by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Auto-orbit when not dragging
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            val startAngle = currentAngle
            val startTime = System.nanoTime()
            val speed = (2 * PI / 30.0).toFloat()

            while (isActive && !isDragging) {
                val elapsed = (System.nanoTime() - startTime) / 1_000_000_000f
                currentAngle = startAngle + elapsed * speed
                kotlinx.coroutines.delay(16)
            }
        }
    }

    val iconSizeDp = 46.dp
    val iconSizePx = with(density) { iconSizeDp.toPx() }
    val hitZonePx = with(density) { 60.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                boxWidth = coords.size.width.toFloat()
                boxHeight = coords.size.height.toFloat()
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)

                    // Check if finger is near the orbit ring
                    val cx = boxWidth / 2f
                    val cy = boxHeight / 2f
                    val orbitR = min(boxWidth, boxHeight) * 0.30f
                    val dx = down.position.x - cx
                    val dy = down.position.y - cy
                    val dist = sqrt(dx * dx + dy * dy)

                    // Only capture if within hitZone of the ring
                    if (abs(dist - orbitR) > hitZonePx) return@awaitEachGesture

                    down.consume()
                    isDragging = true

                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break

                            if (!change.pressed) break

                            val pos = change.position
                            val prev = change.previousPosition
                            val prevA = atan2(prev.y - cy, prev.x - cx)
                            val newA = atan2(pos.y - cy, pos.x - cx)
                            var delta = newA - prevA

                            if (delta > PI) delta -= (2 * PI).toFloat()
                            if (delta < -PI) delta += (2 * PI).toFloat()

                            currentAngle += delta
                            change.consume()
                        }
                    } finally {
                        isDragging = false
                    }
                }
            }
    ) {
        if (boxWidth <= 0f || boxHeight <= 0f) return@Box

        val centerX = boxWidth / 2f
        val centerY = boxHeight / 2f
        val orbitRadius = min(boxWidth, boxHeight) * 0.30f
        val count = favoritePackages.size
        val angleStep = (2 * PI / count).toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = hudColors.accent.copy(alpha = 0.06f),
                center = Offset(centerX, centerY),
                radius = orbitRadius,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        favoritePackages.forEachIndexed { index, packageName ->
            val itemAngle = currentAngle + (angleStep * index)
            val x = centerX + orbitRadius * cos(itemAngle) - iconSizePx / 2f
            val y = centerY + orbitRadius * sin(itemAngle) - iconSizePx / 2f

            val icon: Drawable? = remember(packageName) {
                try {
                    context.packageManager.getApplicationIcon(packageName)
                } catch (_: PackageManager.NameNotFoundException) {
                    null
                }
            }

            icon?.let {
                val bitmap = remember(it) {
                    it.toBitmap(96, 96).asImageBitmap()
                }

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                        .size(iconSizeDp)
                        .clip(CircleShape)
                        .clickable { onAppClick(packageName) },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    hudColors.accent.copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                radius = size.minDimension / 2f
                            ),
                            radius = size.minDimension / 2f
                        )
                    }

                    Image(
                        bitmap = bitmap,
                        contentDescription = packageName,
                        modifier = Modifier
                            .size(iconSizeDp * 0.75f)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}
