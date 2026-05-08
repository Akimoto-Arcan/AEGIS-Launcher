package com.jarvis.launcher.domain.voice

import android.util.Log
import com.jarvis.launcher.data.repository.AssistantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class VoiceState {
    data object Idle : VoiceState()
    data object WakeWordDetected : VoiceState()
    data object Listening : VoiceState()
    data class Recognizing(val partialText: String) : VoiceState()
    data class Processing(val userText: String) : VoiceState()
    data class Speaking(val userText: String, val responseText: String) : VoiceState()
    data object WaitingForFollowUp : VoiceState()
    data class Error(val message: String) : VoiceState()
}

@Singleton
class VoicePipeline @Inject constructor(
    private val wakeWordDetector: WakeWordDetector,
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val assistantRepository: AssistantRepository,
    private val ttsManager: TextToSpeechManager,
    private val audioRouter: AudioRouter
) {
    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    val partialText = speechRecognizerManager.partialText
    val detections = wakeWordDetector.detections

    private val dismissPhrases = listOf(
        "stop", "close", "dismiss", "goodbye", "bye",
        "go away", "shut down", "that's all", "thank you jarvis",
        "thanks jarvis", "never mind", "cancel"
    )

    suspend fun onWakeWordDetected() {
        if (_state.value != VoiceState.Idle &&
            _state.value !is VoiceState.WakeWordDetected &&
            _state.value !is VoiceState.WaitingForFollowUp
        ) return

        Log.d("VoicePipeline", "Activated")
        _state.value = VoiceState.WakeWordDetected
        audioRouter.startBluetoothSco()
        delay(300)

        // Conversation loop — stays open until dismissed
        conversationLoop()

        audioRouter.stopBluetoothSco()
        delay(300)
        _state.value = VoiceState.Idle
    }

    private suspend fun conversationLoop() {
        while (true) {
            _state.value = VoiceState.Listening
            Log.d("VoicePipeline", "Listening...")

            val userSpeech = speechRecognizerManager.recognize()
            Log.d("VoicePipeline", "Heard: $userSpeech")

            if (userSpeech.isNullOrBlank()) {
                _state.value = VoiceState.WaitingForFollowUp
                delay(1500)
                // Silence — listen again
                continue
            }

            val cleaned = stripWakeWord(userSpeech)

            // Check for dismiss commands
            if (dismissPhrases.any { cleaned.lowercase().trim() == it ||
                        cleaned.lowercase().trim().startsWith("$it ") }) {
                _state.value = VoiceState.Speaking(cleaned, "Very well, sir.")
                ttsManager.speak("Very well, sir.")
                delay(500)
                return
            }

            _state.value = VoiceState.Processing(cleaned)
            Log.d("VoicePipeline", "Sending to LLM: $cleaned")

            try {
                val response = withContext(Dispatchers.IO) {
                    assistantRepository.chat(cleaned)
                }
                Log.d("VoicePipeline", "Response: $response")

                _state.value = VoiceState.Speaking(cleaned, response)
                ttsManager.speak(response)
            } catch (e: Exception) {
                Log.e("VoicePipeline", "LLM error", e)
                val errorMsg = "I apologize, sir. I'm having trouble connecting to my systems."
                _state.value = VoiceState.Speaking(cleaned, errorMsg)
                ttsManager.speak(errorMsg)
            }

            // After speaking, wait for follow-up
            _state.value = VoiceState.WaitingForFollowUp
            delay(500)
        }
    }

    fun manualActivate() {
        _state.value = VoiceState.WakeWordDetected
    }

    fun cancel() {
        speechRecognizerManager.cancel()
        ttsManager.stop()
        audioRouter.stopBluetoothSco()
        _state.value = VoiceState.Idle
    }

    private fun stripWakeWord(text: String): String {
        val lower = text.lowercase().trim()
        val prefixes = listOf("jarvis ", "hey jarvis ", "ok jarvis ", "okay jarvis ")
        for (prefix in prefixes) {
            if (lower.startsWith(prefix)) {
                return text.substring(prefix.length).trim()
            }
        }
        return text
    }
}
