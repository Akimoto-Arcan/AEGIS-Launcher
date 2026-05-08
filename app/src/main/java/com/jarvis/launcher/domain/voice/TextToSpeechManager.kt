package com.jarvis.launcher.domain.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
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
                selectJarvisVoice()
                tts?.setPitch(0.85f)
                tts?.setSpeechRate(1.0f)
                _isReady.value = true
            }
        }
    }

    private fun selectJarvisVoice() {
        val voices = tts?.voices ?: return

        Log.d("TTS", "Available voices: ${voices.map { "${it.name} [${it.locale}]" }}")

        // Known male British voice identifiers across TTS engines
        val knownMaleVoices = listOf(
            "en-gb-x-rjs",    // Google TTS male British (Ryan-like)
            "en-gb-x-gbd",    // Google TTS male British variant
            "en-gb-x-fis",    // Google TTS male British variant
            "en-GB-male",
            "eng-gbr-male",
            "Brian",           // Samsung TTS male British
        )

        val englishGbVoices = voices.filter {
            it.locale.language == "en" &&
            (it.locale.country == "GB" || it.locale.country == "UK" || it.locale.country == "")
        }

        // Priority 1: Known male British voice by name substring
        val knownMale = englishGbVoices.firstOrNull { voice ->
            knownMaleVoices.any { id -> voice.name.contains(id, ignoreCase = true) }
        }
        if (knownMale != null) {
            tts?.voice = knownMale
            Log.d("TTS", "Selected known male voice: ${knownMale.name}")
            return
        }

        // Priority 2: Any en-GB voice with "male" in the name
        val maleByName = englishGbVoices.firstOrNull {
            it.name.contains("male", ignoreCase = true) &&
            !it.name.contains("female", ignoreCase = true)
        }
        if (maleByName != null) {
            tts?.voice = maleByName
            Log.d("TTS", "Selected male-named voice: ${maleByName.name}")
            return
        }

        // Priority 3: Any en-GB voice with features containing "male"
        val maleByFeature = englishGbVoices.firstOrNull { voice ->
            voice.features?.any { it.contains("male", ignoreCase = true) } == true
        }
        if (maleByFeature != null) {
            tts?.voice = maleByFeature
            Log.d("TTS", "Selected male-featured voice: ${maleByFeature.name}")
            return
        }

        // Priority 4: en-GB local voice (prefer non-network for speed)
        val localGb = englishGbVoices
            .filter { !it.isNetworkConnectionRequired }
            .sortedBy { it.quality }
            .firstOrNull()
        if (localGb != null) {
            tts?.voice = localGb
            Log.d("TTS", "Selected local en-GB voice: ${localGb.name}")
            return
        }

        // Priority 5: Any en-US male voice as last resort
        val anyMale = voices.filter {
            it.locale.language == "en"
        }.firstOrNull { voice ->
            knownMaleVoices.any { id -> voice.name.contains(id, ignoreCase = true) } ||
            (voice.name.contains("male", ignoreCase = true) &&
             !voice.name.contains("female", ignoreCase = true))
        }
        if (anyMale != null) {
            tts?.voice = anyMale
            Log.d("TTS", "Selected any-English male voice: ${anyMale.name}")
            return
        }

        Log.d("TTS", "No male voice found, using default en-GB")
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
                TextToSpeech.Engine.KEY_PARAM_STREAM,
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
