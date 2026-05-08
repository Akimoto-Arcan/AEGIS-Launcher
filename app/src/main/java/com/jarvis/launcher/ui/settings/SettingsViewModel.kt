package com.jarvis.launcher.ui.settings

import androidx.lifecycle.ViewModel
import com.jarvis.launcher.data.local.ApiKeyStore
import com.jarvis.launcher.data.local.PreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyStore: ApiKeyStore,
    private val preferencesStore: PreferencesStore
) : ViewModel() {

    val apiKey: StateFlow<String> = apiKeyStore.apiKey
    val favoriteApps: StateFlow<List<String>> = preferencesStore.favoriteApps
    val colorThemeName: StateFlow<String> = preferencesStore.colorThemeName
    val useFahrenheit: StateFlow<Boolean> = preferencesStore.useFahrenheit

    fun hasApiKey(): Boolean = apiKeyStore.hasApiKey()

    fun saveApiKey(key: String) = apiKeyStore.setApiKey(key)

    fun setColorTheme(name: String) = preferencesStore.setColorThemeName(name)

    fun addFavoriteApp(packageName: String) = preferencesStore.addFavoriteApp(packageName)

    fun removeFavoriteApp(packageName: String) = preferencesStore.removeFavoriteApp(packageName)

    fun setFavoriteApps(packages: List<String>) = preferencesStore.setFavoriteApps(packages)

    fun toggleTemperatureUnit() = preferencesStore.setUseFahrenheit(!preferencesStore.getUseFahrenheit())
}
