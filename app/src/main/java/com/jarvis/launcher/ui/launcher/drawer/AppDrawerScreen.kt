package com.jarvis.launcher.ui.launcher.drawer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.jarvis.launcher.data.model.AppInfo
import com.jarvis.launcher.ui.theme.HudBackground
import com.jarvis.launcher.ui.theme.HudBorder
import com.jarvis.launcher.ui.theme.HudSurface
import com.jarvis.launcher.ui.theme.HudText
import com.jarvis.launcher.ui.theme.LocalHudColors

@Composable
fun AppDrawerScreen(
    apps: List<AppInfo>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = LocalHudColors.current.accent
    val transition = rememberInfiniteTransition(label = "drawer_scan")
    val scanY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_y"
    )

    Box(modifier = modifier.fillMaxSize().background(HudBackground)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = accent.copy(alpha = 0.1f),
                start = Offset(0f, size.height * scanY),
                end = Offset(size.width, size.height * scanY),
                strokeWidth = 2.dp.toPx()
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "APPLICATIONS",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(HudSurface, RoundedCornerShape(4.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "SEARCH...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HudBorder
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = HudText),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (apps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "LOADING APPLICATIONS..."
                        else "NO RESULTS FOUND",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HudBorder
                    )
                }
            } else {
                AppDrawerGrid(
                    apps = apps,
                    onAppClick = onAppClick
                )
            }
        }
    }
}
