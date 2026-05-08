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
import kotlin.math.roundToInt
import kotlin.math.sin

data class OrbitConfig(
    val radiusXFraction: Float,
    val radiusYFraction: Float,
    val speed: Int,
    val phaseOffset: Float,
    val clockwise: Boolean = true
)

private val orbitConfigs = listOf(
    OrbitConfig(0.34f, 0.30f, 25000, 0f),
    OrbitConfig(0.34f, 0.30f, 25000, PI.toFloat() * 0.25f),
    OrbitConfig(0.34f, 0.30f, 25000, PI.toFloat() * 0.50f),
    OrbitConfig(0.34f, 0.30f, 25000, PI.toFloat() * 0.75f),
    OrbitConfig(0.34f, 0.30f, 25000, PI.toFloat() * 1.0f),
    OrbitConfig(0.34f, 0.30f, 25000, PI.toFloat() * 1.25f),
    OrbitConfig(0.34f, 0.30f, 25000, PI.toFloat() * 1.50f),
    OrbitConfig(0.34f, 0.30f, 25000, PI.toFloat() * 1.75f),
)

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

    val transition = rememberInfiniteTransition(label = "orbit")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    val iconSizeDp = 48.dp
    val iconSizePx = with(density) { iconSizeDp.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        // Draw orbit path ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rx = screenWidthPx * 0.34f
            val ry = screenHeightPx * 0.30f
            drawOval(
                color = hudColors.accent.copy(alpha = 0.08f),
                topLeft = Offset(centerX - rx, centerY - ry),
                size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        favoritePackages.take(8).forEachIndexed { index, packageName ->
            val orbitCfg = orbitConfigs[index % orbitConfigs.size]
            val currentAngle = if (orbitCfg.clockwise) {
                angle + orbitCfg.phaseOffset
            } else {
                -angle + orbitCfg.phaseOffset
            }

            val rx = screenWidthPx * orbitCfg.radiusXFraction
            val ry = screenHeightPx * orbitCfg.radiusYFraction
            val x = centerX + rx * cos(currentAngle) - iconSizePx / 2f
            val y = centerY + ry * sin(currentAngle) - iconSizePx / 2f

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

                val depth = sin(currentAngle)
                val scale = 0.7f + 0.3f * ((depth + 1f) / 2f)
                val alpha = 0.5f + 0.5f * ((depth + 1f) / 2f)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                        .size(iconSizeDp * scale)
                        .clip(CircleShape)
                        .clickable { onAppClick(packageName) },
                    contentAlignment = Alignment.Center
                ) {
                    // Glow ring behind icon
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    hudColors.accent.copy(alpha = 0.3f * alpha),
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
                        alpha = alpha,
                        modifier = Modifier
                            .size(iconSizeDp * scale * 0.75f)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}
