package com.jarvis.launcher.ui.launcher.widgets

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jarvis.launcher.ui.components.GlowText
import com.jarvis.launcher.ui.theme.LocalHudColors
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * HUD-style digital clock displaying HH:MM:SS with a pulsing glow on the seconds.
 *
 * The time updates every second via a coroutine. The seconds portion
 * pulses (alpha oscillates) to provide a subtle "heartbeat" feel.
 *
 * @param modifier Modifier applied to the root Row.
 */
@Composable
fun HudClock(
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    // Tick every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            // Compute delay until next second boundary for accuracy
            val now = System.currentTimeMillis()
            val delayMs = 1000L - (now % 1000L)
            delay(delayMs)
        }
    }

    val hours = currentTime.format(DateTimeFormatter.ofPattern("HH"))
    val minutes = currentTime.format(DateTimeFormatter.ofPattern("mm"))
    val seconds = currentTime.format(DateTimeFormatter.ofPattern("ss"))

    // Seconds pulse animation
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

    val accent = LocalHudColors.current.accent

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        GlowText(text = hours, style = mainStyle, color = accent, glowColor = accent, glowAlpha = 0.35f)
        GlowText(text = ":", style = colonStyle, color = accent, glowColor = accent, glowAlpha = 0.2f)
        GlowText(text = minutes, style = mainStyle, color = accent, glowColor = accent, glowAlpha = 0.35f)
        GlowText(text = ":", style = colonStyle, color = accent.copy(alpha = secondsAlpha), glowColor = accent, glowAlpha = 0.15f)
        GlowText(text = seconds, style = secondsStyle, color = accent.copy(alpha = secondsAlpha), glowColor = accent, glowAlpha = 0.25f * secondsAlpha)
    }
}
