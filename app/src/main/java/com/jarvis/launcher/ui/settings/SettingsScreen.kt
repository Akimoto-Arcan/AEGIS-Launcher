package com.jarvis.launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jarvis.launcher.ui.theme.HudBackground
import com.jarvis.launcher.ui.theme.HudBorder
import com.jarvis.launcher.ui.theme.HudSurface
import com.jarvis.launcher.ui.theme.HudText
import com.jarvis.launcher.ui.theme.JarvisCyan

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentKey by viewModel.apiKey.collectAsState()
    var inputKey by remember { mutableStateOf(currentKey) }
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(systemBarsPadding)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "JARVIS CONFIGURATION",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "OPENROUTER API KEY",
            style = MaterialTheme.typography.labelLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Get a free key at openrouter.ai",
            style = MaterialTheme.typography.bodySmall,
            color = HudBorder
        )

        Spacer(modifier = Modifier.height(12.dp))

        BasicTextField(
            value = inputKey,
            onValueChange = { inputKey = it },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = HudText),
            cursorBrush = SolidColor(JarvisCyan),
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

        Spacer(modifier = Modifier.height(24.dp))

        androidx.compose.material3.Button(
            onClick = {
                viewModel.saveApiKey(inputKey)
                onDone()
            },
            enabled = inputKey.isNotBlank(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = JarvisCyan,
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

        if (currentKey.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))

            androidx.compose.material3.OutlinedButton(
                onClick = onDone,
                border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CANCEL",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your key is stored encrypted on-device and never transmitted to anyone except OpenRouter.",
            style = MaterialTheme.typography.bodySmall,
            color = HudBorder
        )
    }
}
