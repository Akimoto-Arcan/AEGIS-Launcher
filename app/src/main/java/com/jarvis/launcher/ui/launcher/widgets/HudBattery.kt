package com.jarvis.launcher.ui.launcher.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.launcher.ui.theme.JarvisAmber
import com.jarvis.launcher.ui.theme.JarvisCyan

private val BatteryRed = Color(0xFFFF1744)

/**
 * HUD-style circular battery indicator.
 *
 * Features:
 * - Circular arc whose sweep angle corresponds to battery percentage
 * - Percentage text in the centre
 * - Colour coding: cyan >50%, amber 20-50%, red <20%
 * - Updates via a BroadcastReceiver for [Intent.ACTION_BATTERY_CHANGED]
 *
 * @param modifier Modifier for the outer Box.
 * @param indicatorSize Diameter of the circular indicator.
 * @param strokeWidth Width of the arc stroke.
 */
@Composable
fun HudBattery(
    modifier: Modifier = Modifier,
    indicatorSize: Dp = 64.dp,
    strokeWidth: Dp = 3.dp
) {
    val context = LocalContext.current
    var batteryLevel by remember { mutableIntStateOf(getBatteryLevel(context)) }

    // Register broadcast receiver for battery updates
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                    if (level >= 0 && scale > 0) {
                        batteryLevel = (level * 100) / scale
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val batteryColor = when {
        batteryLevel > 50 -> JarvisCyan
        batteryLevel > 20 -> JarvisAmber
        else -> BatteryRed
    }

    val sweepAngle = (batteryLevel / 100f) * 360f

    Box(
        modifier = modifier.size(indicatorSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(indicatorSize)) {
            val sw = strokeWidth.toPx()
            val padding = sw / 2f
            val arcSize = Size(size.width - sw, size.height - sw)
            val arcTopLeft = Offset(padding, padding)

            // Background track
            drawArc(
                color = batteryColor.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = sw, cap = StrokeCap.Round)
            )

            // Foreground arc
            drawArc(
                color = batteryColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = sw, cap = StrokeCap.Round)
            )
        }

        // Percentage text
        Text(
            text = "$batteryLevel%",
            color = batteryColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Reads the current battery level synchronously from a sticky broadcast.
 */
private fun getBatteryLevel(context: Context): Int {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    return if (intent != null) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        if (level >= 0 && scale > 0) (level * 100) / scale else 50
    } else {
        50
    }
}
