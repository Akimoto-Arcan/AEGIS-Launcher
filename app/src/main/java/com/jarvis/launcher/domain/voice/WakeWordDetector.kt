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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
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
    private var isListening = false
    private var enabled = false

    suspend fun startListening() = withContext(Dispatchers.Main) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("WakeWord", "Speech recognition not available")
            return@withContext
        }

        enabled = true
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }

            override fun onError(error: Int) {
                isListening = false
                // Restart listening after a brief pause
                if (enabled) {
                    mainHandler.postDelayed({ startListeningInternal() }, 500)
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )
                val text = matches?.firstOrNull()?.lowercase() ?: ""

                if (text.contains("jarvis")) {
                    Log.d("WakeWord", "Detected wake word in: $text")
                    _detections.tryEmit("jarvis")
                    // Pause before restarting to let the pipeline handle it
                    if (enabled) {
                        mainHandler.postDelayed({ startListeningInternal() }, 5000)
                    }
                } else {
                    // Didn't hear "jarvis", keep listening
                    if (enabled) {
                        mainHandler.postDelayed({ startListeningInternal() }, 100)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )
                val text = partial?.firstOrNull()?.lowercase() ?: ""
                if (text.contains("jarvis")) {
                    Log.d("WakeWord", "Detected wake word in partial: $text")
                    recognizer?.cancel()
                    _detections.tryEmit("jarvis")
                    if (enabled) {
                        mainHandler.postDelayed({ startListeningInternal() }, 5000)
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (!enabled) return
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
            }
            recognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("WakeWord", "Failed to start listening", e)
            if (enabled) {
                mainHandler.postDelayed({ startListeningInternal() }, 2000)
            }
        }
    }

    fun stopListening() {
        enabled = false
        isListening = false
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
