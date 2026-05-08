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

    suspend fun onWakeWordDetected() {
        if (_state.value != VoiceState.Idle && _state.value !is VoiceState.WakeWordDetected) return

        Log.d("VoicePipeline", "Activated, starting pipeline")
        _state.value = VoiceState.WakeWordDetected

        // Route audio through Bluetooth if connected
        audioRouter.startBluetoothSco()
        delay(500)

        _state.value = VoiceState.Listening
        Log.d("VoicePipeline", "Listening for speech...")

        val userSpeech = speechRecognizerManager.recognize()
        Log.d("VoicePipeline", "Speech result: $userSpeech")

        if (userSpeech.isNullOrBlank()) {
            _state.value = VoiceState.Error("I didn't catch that, sir.")
            delay(2000)
            audioRouter.stopBluetoothSco()
            _state.value = VoiceState.Idle
            return
        }

        val cleanedSpeech = stripWakeWord(userSpeech)
        _state.value = VoiceState.Processing(cleanedSpeech)
        Log.d("VoicePipeline", "Sending to LLM: $cleanedSpeech")

        try {
            val response = withContext(Dispatchers.IO) {
                assistantRepository.chat(cleanedSpeech)
            }
            Log.d("VoicePipeline", "LLM response: $response")

            _state.value = VoiceState.Speaking(cleanedSpeech, response)
            ttsManager.speak(response)
        } catch (e: Exception) {
            Log.e("VoicePipeline", "LLM error", e)
            val errorMsg = "I apologize, sir. I'm having trouble connecting to my systems."
            _state.value = VoiceState.Speaking(cleanedSpeech, errorMsg)
            ttsManager.speak(errorMsg)
        }

        audioRouter.stopBluetoothSco()
        delay(500)
        _state.value = VoiceState.Idle
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
