package com.jarvis.launcher.domain.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
    @ApplicationContext private val context: Context
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
                tts?.setPitch(0.9f)
                tts?.setSpeechRate(1.05f)
                selectBestVoice()
                _isReady.value = true
            }
        }
    }

    private fun selectBestVoice() {
        val voices = tts?.voices ?: return
        val preferred = voices.filter {
            it.locale.language == "en" &&
                (it.locale.country == "GB" || it.locale.country == "UK") &&
                !it.isNetworkConnectionRequired
        }.minByOrNull { it.quality }

        if (preferred != null) {
            tts?.voice = preferred
        } else {
            val fallback = voices.filter {
                it.locale.language == "en" && !it.isNetworkConnectionRequired
            }.minByOrNull { it.quality }
            fallback?.let { tts?.voice = it }
        }
    }

    suspend fun speak(text: String) = suspendCancellableCoroutine { cont ->
        if (tts == null || !_isReady.value) {
            cont.resume(Unit)
            return@suspendCancellableCoroutine
        }

        val utteranceId = UUID.randomUUID().toString()
        _isSpeaking.value = true

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    _isSpeaking.value = false
                    cont.resume(Unit)
                }
            }

            @Deprecated("Deprecated in API")
            override fun onError(id: String?) {
                if (id == utteranceId) {
                    _isSpeaking.value = false
                    cont.resume(Unit)
                }
            }
        })

        val params = android.os.Bundle().apply {
            putInt(
                android.speech.tts.TextToSpeech.Engine.KEY_PARAM_STREAM,
                android.media.AudioManager.STREAM_MUSIC
            )
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

        cont.invokeOnCancellation {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
    }
}
