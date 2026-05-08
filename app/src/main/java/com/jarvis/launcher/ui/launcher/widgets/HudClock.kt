package com.jarvis.launcher.ui.launcher.widgets

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.launcher.ui.components.GlowText
import com.jarvis.launcher.ui.theme.HudTextDim
import com.jarvis.launcher.ui.theme.LocalHudColors
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HudClock(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accent = LocalHudColors.current.accent
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var nextAlarmText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            nextAlarmText = getNextAlarmText(context)
            val now = System.currentTimeMillis()
            delay(1000L - (now % 1000L))
        }
    }

    val hours = currentTime.format(DateTimeFormatter.ofPattern("HH"))
    val minutes = currentTime.format(DateTimeFormatter.ofPattern("mm"))
    val seconds = currentTime.format(DateTimeFormatter.ofPattern("ss"))

    val infiniteTransition = rememberInfiniteTransition(label = "clock_pulse")
    val secondsAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "seconds_alpha"
    )

    val mainStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Light,
        fontSize = 56.sp,
        letterSpacing = 4.sp
    )
    val secondsStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Light,
        fontSize = 36.sp,
        letterSpacing = 2.sp
    )
    val colonStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Light,
        fontSize = 56.sp,
        letterSpacing = 2.sp
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { openClockApp(context) }
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            GlowText(text = hours, style = mainStyle, color = accent, glowColor = accent, glowAlpha = 0.35f)
            GlowText(text = ":", style = colonStyle, color = accent, glowColor = accent, glowAlpha = 0.2f)
            GlowText(text = minutes, style = mainStyle, color = accent, glowColor = accent, glowAlpha = 0.35f)
            GlowText(text = ":", style = colonStyle, color = accent.copy(alpha = secondsAlpha), glowColor = accent, glowAlpha = 0.15f)
            GlowText(text = seconds, style = secondsStyle, color = accent.copy(alpha = secondsAlpha), glowColor = accent, glowAlpha = 0.25f * secondsAlpha)
        }

        nextAlarmText?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                color = HudTextDim,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

private fun openClockApp(context: Context) {
    val intents = listOf(
        Intent(AlarmClock.ACTION_SHOW_ALARMS),
        Intent(Intent.ACTION_MAIN).apply {
            setClassName("com.sec.android.app.clockpackage", "com.sec.android.app.clockpackage.ClockPackage")
        },
        Intent(Intent.ACTION_MAIN).apply {
            setClassName("com.google.android.deskclock", "com.android.deskclock.DeskClock")
        }
    )
    for (intent in intents) {
        try {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        } catch (_: Exception) {}
    }
}

private val clockPackages = setOf(
    "com.sec.android.app.clockpackage",
    "com.samsung.android.app.clockpackage",
    "com.google.android.deskclock",
    "com.android.deskclock",
    "com.oneplus.deskclock",
    "com.coloros.alarmclock",
    "com.oppo.alarmclock",
)

private fun getNextAlarmText(context: Context): String? {
    try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return null
        val alarmInfo = alarmManager.nextAlarmClock ?: return null

        // Only show alarms from actual clock apps, not random app timers
        val showIntent = alarmInfo.showIntent
        val alarmPackage = showIntent?.creatorPackage
        if (alarmPackage != null && alarmPackage !in clockPackages) return null

        val triggerTime = alarmInfo.triggerTime
        val now = System.currentTimeMillis()

        if (triggerTime <= now) return null

        val diffMs = triggerTime - now
        val diffHours = diffMs / (1000 * 60 * 60)
        if (diffHours > 24) return null

        val alarmTime = Instant.ofEpochMilli(triggerTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()

        val formatted = alarmTime.format(DateTimeFormatter.ofPattern("h:mm a"))

        val totalMinutes = diffMs / (1000 * 60)
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        val timeUntil = when {
            h == 0L -> "${m}m"
            m == 0L -> "${h}h"
            else -> "${h}h ${m}m"
        }

        return "ALARM $formatted ($timeUntil)"
    } catch (_: Exception) {
        return null
    }
}
