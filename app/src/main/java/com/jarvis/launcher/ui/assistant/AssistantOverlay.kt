package com.jarvis.launcher.ui.assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jarvis.launcher.domain.voice.VoiceState
import com.jarvis.launcher.ui.theme.HudBackground
import com.jarvis.launcher.ui.theme.JarvisCyan
import com.jarvis.launcher.ui.theme.JarvisAmber

@Composable
fun AssistantOverlay(viewModel: AssistantViewModel) {
    val voiceState by viewModel.voiceState.collectAsState()
    val partialText by viewModel.partialText.collectAsState()
    val isVisible = voiceState !is VoiceState.Idle

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f),
        exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.8f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HudBackground.copy(alpha = 0.85f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { viewModel.onCancel() },
            contentAlignment = Alignment.Center
        ) {
            HudFrame()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "J.A.R.V.I.S.",
                    style = MaterialTheme.typography.titleLarge,
                    color = JarvisCyan,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = statusText(voiceState),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor(voiceState),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                CircularVisualizer(
                    isActive = voiceState is VoiceState.Listening ||
                            voiceState is VoiceState.Speaking
                )

                Spacer(modifier = Modifier.height(24.dp))

                when (val state = voiceState) {
                    is VoiceState.Listening -> {
                        WaveformVisualizer(isActive = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (partialText.isNotEmpty()) {
                            TranscriptDisplay(
                                userText = partialText,
                                responseText = ""
                            )
                        }
                    }

                    is VoiceState.Recognizing -> {
                        TranscriptDisplay(
                            userText = state.partialText,
                            responseText = ""
                        )
                    }

                    is VoiceState.Processing -> {
                        TranscriptDisplay(
                            userText = state.userText,
                            responseText = ""
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        WaveformVisualizer(isActive = false)
                    }

                    is VoiceState.Speaking -> {
                        WaveformVisualizer(isActive = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        TranscriptDisplay(
                            userText = state.userText,
                            responseText = state.responseText
                        )
                    }

                    is VoiceState.WaitingForFollowUp -> {
                        WaveformVisualizer(isActive = false)
                        Spacer(modifier = Modifier.height(16.dp))
                        TranscriptDisplay(
                            userText = "",
                            responseText = "Awaiting further instructions, sir..."
                        )
                    }

                    is VoiceState.Error -> {
                        TranscriptDisplay(
                            userText = "",
                            responseText = state.message
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}

private fun statusText(state: VoiceState): String = when (state) {
    is VoiceState.WakeWordDetected -> "INITIALIZING..."
    is VoiceState.Listening -> "LISTENING"
    is VoiceState.Recognizing -> "PROCESSING SPEECH"
    is VoiceState.Processing -> "ANALYZING REQUEST"
    is VoiceState.Speaking -> "RESPONDING"
    is VoiceState.WaitingForFollowUp -> "STANDING BY"
    is VoiceState.Error -> "SYSTEM ERROR"
    is VoiceState.Idle -> ""
}

private fun statusColor(state: VoiceState): androidx.compose.ui.graphics.Color = when (state) {
    is VoiceState.Error -> JarvisAmber
    is VoiceState.Speaking -> JarvisCyan
    else -> JarvisCyan.copy(alpha = 0.7f)
}
