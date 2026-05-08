package com.jarvis.launcher.domain.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeWordDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _detections = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val detections: SharedFlow<String> = _detections.asSharedFlow()

    private var recognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var enabled = false

    private val wakePatterns = listOf(
        "jarvis",
        "hey jarvis",
        "ok jarvis",
        "okay jarvis"
    )

    suspend fun startListening() = withContext(Dispatchers.Main) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return@withContext

        enabled = true
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                if (enabled) {
                    val delay = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> 300L
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> 2000L
                        else -> 1000L
                    }
                    mainHandler.postDelayed({ restartListening() }, delay)
                }
            }

            override fun onResults(results: Bundle?) {
                checkForWakeWord(results)
                if (enabled) {
                    mainHandler.postDelayed({ restartListening() }, 200)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Only check partials — don't trigger and restart here
                // to avoid double-firing
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        restartListening()
    }

    private fun checkForWakeWord(results: Bundle?) {
        val matches = results?.getStringArrayList(
            SpeechRecognizer.RESULTS_RECOGNITION
        ) ?: return
        val confidences = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

        for ((index, text) in matches.withIndex()) {
            val lower = text.lowercase().trim()
            val confidence = confidences?.getOrNull(index) ?: 0f

            // Strict matching: the utterance must BE the wake word
            // or START with the wake word (e.g. "Jarvis what time is it")
            val isWakeWord = wakePatterns.any { pattern ->
                lower == pattern ||
                lower.startsWith("$pattern ") ||
                lower.startsWith("$pattern,")
            }

            if (isWakeWord && (confidence > 0.5f || confidences == null)) {
                Log.d("WakeWord", "Detected: '$text' (confidence: $confidence)")
                _detections.tryEmit("jarvis")
                // Pause detection while pipeline runs
                if (enabled) {
                    enabled = false
                    mainHandler.postDelayed({
                        enabled = true
                        restartListening()
                    }, 8000)
                }
                return
            }
        }
    }

    private fun restartListening() {
        if (!enabled) return
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                    2000L
                )
            }
            recognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("WakeWord", "Failed to start", e)
            if (enabled) {
                mainHandler.postDelayed({ restartListening() }, 3000)
            }
        }
    }

    fun stopListening() {
        enabled = false
        mainHandler.post {
            try {
                recognizer?.cancel()
                recognizer?.destroy()
                recognizer = null
            } catch (_: Exception) {}
        }
    }

    fun triggerManually() {
        _detections.tryEmit("jarvis")
    }
}
