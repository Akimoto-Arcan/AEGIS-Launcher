package com.jarvis.launcher.ui.settings

import androidx.lifecycle.ViewModel
import com.jarvis.launcher.data.local.ApiKeyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyStore: ApiKeyStore
) : ViewModel() {

    val apiKey: StateFlow<String> = apiKeyStore.apiKey

    fun hasApiKey(): Boolean = apiKeyStore.hasApiKey()

    fun saveApiKey(key: String) {
        apiKeyStore.setApiKey(key)
    }
}
