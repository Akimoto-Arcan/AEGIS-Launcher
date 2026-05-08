package com.jarvis.launcher.ui.launcher.home

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.jarvis.launcher.ui.theme.LocalHudColors
import kotlin.math.PI
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
    val config = LocalConfiguration.current
    val hudColors = LocalHudColors.current

    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }
    val centerX = screenWidthPx / 2f
    val centerY = screenHeightPx / 2f
    val orbitRadius = min(screenWidthPx, screenHeightPx) * 0.33f

    val transition = rememberInfiniteTransition(label = "orbit")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    val iconSizeDp = 46.dp
    val iconSizePx = with(density) { iconSizeDp.toPx() }
    val count = favoritePackages.size
    val angleStep = (2 * PI / count).toFloat()

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = hudColors.accent.copy(alpha = 0.06f),
                center = Offset(centerX, centerY),
                radius = orbitRadius,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        favoritePackages.forEachIndexed { index, packageName ->
            val itemAngle = angle + (angleStep * index)
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
