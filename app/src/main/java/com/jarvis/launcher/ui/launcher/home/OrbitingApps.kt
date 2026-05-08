package com.jarvis.launcher.ui.launcher.home

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.jarvis.launcher.ui.theme.LocalHudColors
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

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

    // The current rotation angle in radians
    var currentAngle by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Auto-orbit: continuously increment angle when not dragging
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            val startAngle = currentAngle
            val startTime = System.nanoTime()
            val speed = (2 * PI / 30.0).toFloat() // full rotation in 30 seconds

            while (isActive && !isDragging) {
                val elapsed = (System.nanoTime() - startTime) / 1_000_000_000f
                currentAngle = startAngle + elapsed * speed
                kotlinx.coroutines.delay(16) // ~60fps
            }
        }
    }

    val iconSizeDp = 46.dp
    val iconSizePx = with(density) { iconSizeDp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                boxWidth = coords.size.width.toFloat()
                boxHeight = coords.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                    },
                    onDragEnd = {
                        isDragging = false
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val cx = boxWidth / 2f
                        val cy = boxHeight / 2f
                        val pos = change.position
                        val prev = change.previousPosition

                        // Calculate angle change from finger movement relative to center
                        val prevAngle = atan2(prev.y - cy, prev.x - cx)
                        val newAngle = atan2(pos.y - cy, pos.x - cx)
                        var delta = newAngle - prevAngle

                        // Normalize delta to avoid jumps at ±π boundary
                        if (delta > PI) delta -= (2 * PI).toFloat()
                        if (delta < -PI) delta += (2 * PI).toFloat()

                        currentAngle += delta
                    }
                )
            }
    ) {
        if (boxWidth <= 0f || boxHeight <= 0f) return@Box

        val centerX = boxWidth / 2f
        val centerY = boxHeight / 2f
        val orbitRadius = min(boxWidth, boxHeight) * 0.30f
        val count = favoritePackages.size
        val angleStep = (2 * PI / count).toFloat()

        // Orbit path ring
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
