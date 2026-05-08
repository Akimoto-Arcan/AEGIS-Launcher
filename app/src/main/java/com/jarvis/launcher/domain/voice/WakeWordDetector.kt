package com.jarvis.launcher.domain.voice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeWordDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _detections = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val detections: SharedFlow<String> = _detections.asSharedFlow()

    @Suppress("unused")
    suspend fun startListening() {
        // Auto wake word detection disabled — too many false triggers
        // without a proper trained model. Activation is tap-only.
    }

    fun stopListening() {}

    fun triggerManually() {
        _detections.tryEmit("jarvis")
    }
}
