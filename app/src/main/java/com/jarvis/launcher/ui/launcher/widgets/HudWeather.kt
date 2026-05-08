package com.jarvis.launcher.ui.launcher.widgets

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.launcher.data.repository.WeatherData
import com.jarvis.launcher.ui.theme.HudTextDim
import com.jarvis.launcher.ui.theme.LocalHudColors
import kotlinx.coroutines.delay

@Composable
fun HudWeather(
    weatherData: WeatherData?,
    useFahrenheit: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = LocalHudColors.current.accent
    val context = LocalContext.current

    Column(
        modifier = modifier
            .clickable {
                val intents = listOf(
                    Intent(Intent.ACTION_VIEW, android.net.Uri.parse("dynact://vel498702545/weather")),
                    Intent(Intent.ACTION_MAIN).apply {
                        setClassName("com.sec.android.daemonapp", "com.sec.android.daemonapp.ap.hero.WeatherActivity")
                    },
                    Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://weather.com"))
                )
                for (intent in intents) {
                    try {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        return@clickable
                    } catch (_: Exception) {}
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (weatherData != null) {
            val temp = if (useFahrenheit) "${weatherData.tempF}°F" else "${weatherData.tempC}°C"

            Text(
                text = temp,
                color = accent,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                fontSize = 22.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = weatherData.condition,
                color = HudTextDim,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = weatherData.location,
                color = HudTextDim.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 8.sp,
                letterSpacing = 1.sp
            )
        } else {
            Text(
                text = "-- °F",
                color = accent.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                fontSize = 22.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "LOADING...",
                color = HudTextDim,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
