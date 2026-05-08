package com.jarvis.launcher.ui.launcher.widgets

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.jarvis.launcher.ui.theme.HudTextDim
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * HUD-style date display showing day-of-week, day, month, and year in uppercase
 * monospace text.
 *
 * Updates once per minute to avoid unnecessary recomposition.
 *
 * @param modifier Modifier applied to the Text.
 */
@Composable
fun HudDate(
    modifier: Modifier = Modifier
) {
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    // Refresh once per minute (date changes are rare but we keep it alive)
    LaunchedEffect(Unit) {
        while (true) {
            currentDate = LocalDate.now()
            delay(60_000L)
        }
    }

    val formatter = remember {
        DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.ENGLISH)
    }

    val dateText = currentDate.format(formatter).uppercase(Locale.ENGLISH)

    Text(
        text = dateText,
        modifier = modifier,
        color = HudTextDim,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 2.sp,
        textAlign = TextAlign.Center
    )
}
