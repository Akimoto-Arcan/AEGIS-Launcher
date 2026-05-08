package com.jarvis.launcher.domain.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val edgeTts: EdgeTtsEngine,
    private val preferencesStore: com.jarvis.launcher.data.local.PreferencesStore
) {
    private var tts: TextToSpeech? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    fun initialize() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.UK
                selectFallbackVoice()
                tts?.setPitch(0.85f)
                tts?.setSpeechRate(1.0f)
                _isReady.value = true
            }
        }
    }

    private fun selectFallbackVoice() {
        val voices = tts?.voices ?: return
        val knownMale = listOf("en-gb-x-rjs", "en-gb-x-gbd", "Brian", "en-GB-male")
        val gbVoices = voices.filter {
            it.locale.language == "en" &&
            (it.locale.country == "GB" || it.locale.country == "UK")
        }
        val male = gbVoices.firstOrNull { voice ->
            knownMale.any { voice.name.contains(it, ignoreCase = true) }
        } ?: gbVoices.firstOrNull { !it.isNetworkConnectionRequired }

        male?.let { tts?.voice = it }
    }

    suspend fun speak(text: String) {
        _isSpeaking.value = true
        try {
            // Try Edge TTS first (AI neural voice)
            val voice = preferencesStore.getVoiceId()
            val success = edgeTts.speak(text, voice)
            if (success) {
                Log.d("TTS", "Spoke via Edge TTS (en-GB-RyanNeural)")
                return
            }

            // Fall back to Android TTS
            Log.d("TTS", "Edge TTS failed, falling back to Android TTS")
            speakWithAndroidTts(text)
        } finally {
            _isSpeaking.value = false
        }
    }

    private suspend fun speakWithAndroidTts(text: String) = suspendCancellableCoroutine { cont ->
        if (tts == null || !_isReady.value) {
            cont.resume(Unit)
            return@suspendCancellableCoroutine
        }

        val utteranceId = UUID.randomUUID().toString()
        var resumed = false

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(id: String?) {
                if (id == utteranceId && !resumed) {
                    resumed = true
                    cont.resume(Unit)
                }
            }

            @Deprecated("Deprecated in API")
            override fun onError(id: String?) {
                if (id == utteranceId && !resumed) {
                    resumed = true
                    cont.resume(Unit)
                }
            }
        })

        val params = android.os.Bundle().apply {
            putInt(
                TextToSpeech.Engine.KEY_PARAM_STREAM,
                android.media.AudioManager.STREAM_MUSIC
            )
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

        cont.invokeOnCancellation {
            tts?.stop()
        }
    }

    fun stop() {
        edgeTts.stop()
        tts?.stop()
        _isSpeaking.value = false
    }

    fun release() {
        edgeTts.stop()
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
    }
}
