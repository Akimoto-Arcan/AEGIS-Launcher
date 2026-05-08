package com.jarvis.launcher.domain.voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeWordDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var audioRecord: AudioRecord? = null
    private var isListening = false

    private val _detections = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val detections: SharedFlow<String> = _detections.asSharedFlow()

    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(sampleRate * 2)

    private val wakeWords = listOf("jarvis", "hey jarvis", "ok jarvis")

    suspend fun startListening() = withContext(Dispatchers.IO) {
        if (isListening) return@withContext

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext
            }

            audioRecord?.startRecording()
            isListening = true

            val buffer = ShortArray(sampleRate)

            while (isActive && isListening) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                if (read > 0) {
                    val energy = calculateEnergy(buffer, read)
                    if (energy > ENERGY_THRESHOLD) {
                        _detections.tryEmit("jarvis")
                        kotlinx.coroutines.delay(3000)
                    }
                }
            }
        } catch (e: SecurityException) {
            // Microphone permission not granted
        }
    }

    fun stopListening() {
        isListening = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    private fun calculateEnergy(buffer: ShortArray, length: Int): Double {
        var sum = 0.0
        for (i in 0 until length) {
            sum += buffer[i] * buffer[i]
        }
        return sum / length
    }

    companion object {
        private const val ENERGY_THRESHOLD = 5_000_000.0
    }
}
