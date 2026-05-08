package com.jarvis.launcher.ui.settings

import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.jarvis.launcher.data.local.PreferencesStore
import com.jarvis.launcher.data.model.AppInfo
import com.jarvis.launcher.ui.theme.HudBackground
import com.jarvis.launcher.ui.theme.HudBorder
import com.jarvis.launcher.ui.theme.HudSurface
import com.jarvis.launcher.ui.theme.HudText
import com.jarvis.launcher.ui.theme.LocalHudColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    installedApps: List<AppInfo>,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hudColors = LocalHudColors.current
    val currentKey by viewModel.apiKey.collectAsState()
    val favorites by viewModel.favoriteApps.collectAsState()
    val currentTheme by viewModel.colorThemeName.collectAsState()
    var inputKey by remember { mutableStateOf(currentKey) }
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(systemBarsPadding),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AEGIS CONFIGURATION",
                style = MaterialTheme.typography.titleLarge,
                color = hudColors.accent
            )
        }

        // ===== COLOR THEME =====
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "HUD COLOR THEME",
                style = MaterialTheme.typography.labelLarge,
                color = hudColors.accent
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PreferencesStore.COLOR_THEMES.forEach { theme ->
                    val isSelected = theme.name == currentTheme
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            viewModel.setColorTheme(theme.name)
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(theme.primary))
                                .then(
                                    if (isSelected) Modifier.border(
                                        3.dp, Color.White, CircleShape
                                    ) else Modifier
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = theme.name.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else HudBorder
                        )
                    }
                }
            }
        }

        // ===== VOICE =====
        item {
            val currentVoice by viewModel.voiceId.collectAsState()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AEGIS VOICE",
                style = MaterialTheme.typography.labelLarge,
                color = hudColors.accent
            )
            Spacer(modifier = Modifier.height(8.dp))

            com.jarvis.launcher.domain.voice.EdgeTtsEngine.VOICES.forEach { (id, label) ->
                val isSelected = currentVoice == id
                OutlinedButton(
                    onClick = { viewModel.setVoice(id) },
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) hudColors.accent else HudBorder
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = if (isSelected) "$label  ●" else label,
                        color = if (isSelected) hudColors.accent else HudBorder,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // ===== ACTIVATION MODE =====
        item {
            val alwaysListening by viewModel.alwaysListening.collectAsState()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ACTIVATION MODE",
                style = MaterialTheme.typography.labelLarge,
                color = hudColors.accent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (alwaysListening) "Always listening for \"Aegis\""
                else "Tap only — no background listening",
                style = MaterialTheme.typography.bodySmall,
                color = HudBorder
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(false to "TAP ONLY", true to "ALWAYS LISTEN").forEach { (mode, label) ->
                    val isSelected = alwaysListening == mode
                    OutlinedButton(
                        onClick = { if (!isSelected) viewModel.toggleAlwaysListening() },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) hudColors.accent else HudBorder
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) hudColors.accent else HudBorder,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // ===== TEMPERATURE UNIT =====
        item {
            val useFahrenheit by viewModel.useFahrenheit.collectAsState()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "TEMPERATURE UNIT",
                style = MaterialTheme.typography.labelLarge,
                color = hudColors.accent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(true to "°F", false to "°C").forEach { (isFahrenheit, label) ->
                    val isSelected = useFahrenheit == isFahrenheit
                    OutlinedButton(
                        onClick = { if (!isSelected) viewModel.toggleTemperatureUnit() },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) hudColors.accent else HudBorder
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) hudColors.accent else HudBorder,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // ===== ORBIT APPS =====
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ORBIT APPS (${favorites.size}/${PreferencesStore.MAX_FAVORITES})",
                style = MaterialTheme.typography.labelLarge,
                color = hudColors.accent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select apps to orbit the reactor",
                style = MaterialTheme.typography.bodySmall,
                color = HudBorder
            )
        }

        item {
            // Current favorites
            if (favorites.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    favorites.forEach { pkg ->
                        val label = remember(pkg) {
                            try {
                                context.packageManager
                                    .getApplicationLabel(
                                        context.packageManager.getApplicationInfo(pkg, 0)
                                    ).toString()
                            } catch (_: PackageManager.NameNotFoundException) { pkg }
                        }
                        OutlinedButton(
                            onClick = { viewModel.removeFavoriteApp(pkg) },
                            border = BorderStroke(1.dp, hudColors.accent),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$label  ✕",
                                style = MaterialTheme.typography.labelSmall,
                                color = hudColors.accent
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // App grid for selection
        item {
            val available = installedApps.filter { it.packageName !in favorites }
            if (available.isNotEmpty()) {
                Box(modifier = Modifier.height(280.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(available, key = { it.packageName }) { app ->
                            val canAdd = favorites.size < PreferencesStore.MAX_FAVORITES
                            val icon = remember(app.packageName) {
                                try {
                                    context.packageManager
                                        .getApplicationIcon(app.packageName)
                                        .toBitmap(72, 72).asImageBitmap()
                                } catch (_: Exception) { null }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (canAdd) HudSurface else HudSurface.copy(alpha = 0.3f))
                                    .clickable(enabled = canAdd) {
                                        viewModel.addFavoriteApp(app.packageName)
                                    }
                                    .padding(8.dp)
                            ) {
                                icon?.let {
                                    Image(
                                        bitmap = it,
                                        contentDescription = app.label,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    color = if (canAdd) HudText else HudBorder
                                )
                            }
                        }
                    }
                }
            }
        }

        // ===== API KEY =====
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "OPENROUTER API KEY",
                style = MaterialTheme.typography.labelLarge,
                color = hudColors.accent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Get a free key at openrouter.ai",
                style = MaterialTheme.typography.bodySmall,
                color = HudBorder
            )
            Spacer(modifier = Modifier.height(8.dp))

            BasicTextField(
                value = inputKey,
                onValueChange = { inputKey = it },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = HudText),
                cursorBrush = SolidColor(hudColors.accent),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HudSurface, RoundedCornerShape(4.dp))
                    .padding(16.dp),
                decorationBox = { innerTextField ->
                    if (inputKey.isEmpty()) {
                        Text(
                            text = "sk-or-v1-...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HudBorder
                        )
                    }
                    innerTextField()
                }
            )
        }

        // ===== SAVE / CANCEL =====
        item {
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (inputKey.isNotBlank()) viewModel.saveApiKey(inputKey)
                    onDone()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = hudColors.accent,
                    contentColor = HudBackground
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SAVE AND CONTINUE",
                    style = MaterialTheme.typography.labelLarge.copy(color = HudBackground)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDone,
                border = BorderStroke(1.dp, HudBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CANCEL",
                    style = MaterialTheme.typography.labelLarge,
                    color = hudColors.accent
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your API key is stored encrypted on-device.",
                style = MaterialTheme.typography.bodySmall,
                color = HudBorder
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
