package com.jarvis.launcher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.launcher.data.local.ApiKeyStore
import com.jarvis.launcher.data.local.PreferencesStore
import com.jarvis.launcher.domain.voice.EdgeTtsEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyStore: ApiKeyStore,
    private val preferencesStore: PreferencesStore,
    private val edgeTts: EdgeTtsEngine
) : ViewModel() {

    val apiKey: StateFlow<String> = apiKeyStore.apiKey
    val favoriteApps: StateFlow<List<String>> = preferencesStore.favoriteApps
    val colorThemeName: StateFlow<String> = preferencesStore.colorThemeName
    val useFahrenheit: StateFlow<Boolean> = preferencesStore.useFahrenheit
    val voiceId: StateFlow<String> = preferencesStore.voiceId
    val alwaysListening: StateFlow<Boolean> = preferencesStore.alwaysListening

    fun hasApiKey(): Boolean = apiKeyStore.hasApiKey()

    fun saveApiKey(key: String) = apiKeyStore.setApiKey(key)

    fun setColorTheme(name: String) = preferencesStore.setColorThemeName(name)

    fun addFavoriteApp(packageName: String) = preferencesStore.addFavoriteApp(packageName)

    fun removeFavoriteApp(packageName: String) = preferencesStore.removeFavoriteApp(packageName)

    fun setFavoriteApps(packages: List<String>) = preferencesStore.setFavoriteApps(packages)

    fun toggleTemperatureUnit() = preferencesStore.setUseFahrenheit(!preferencesStore.getUseFahrenheit())

    fun setVoice(id: String) {
        preferencesStore.setVoiceId(id)
        previewVoice(id)
    }

    fun previewVoice(voiceId: String) {
        viewModelScope.launch {
            edgeTts.stop()
            edgeTts.speak("Hello, I am Aegis.", voiceId)
        }
    }

    fun toggleAlwaysListening() = preferencesStore.setAlwaysListening(!preferencesStore.getAlwaysListening())
}
