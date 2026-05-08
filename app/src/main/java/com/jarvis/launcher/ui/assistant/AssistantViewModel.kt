package com.jarvis.launcher.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.launcher.domain.voice.TextToSpeechManager
import com.jarvis.launcher.domain.voice.VoicePipeline
import com.jarvis.launcher.domain.voice.VoiceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val voicePipeline: VoicePipeline,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {

    init {
        ttsManager.initialize()
    }

    val voiceState: StateFlow<VoiceState> = voicePipeline.state

    val partialText: StateFlow<String> = voicePipeline.partialText
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun onWakeWordDetected() {
        viewModelScope.launch {
            voicePipeline.onWakeWordDetected()
        }
    }

    fun onManualActivate() {
        viewModelScope.launch {
            voicePipeline.manualActivate()
            voicePipeline.onWakeWordDetected()
        }
    }

    fun onCancel() {
        voicePipeline.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.release()
    }
}
