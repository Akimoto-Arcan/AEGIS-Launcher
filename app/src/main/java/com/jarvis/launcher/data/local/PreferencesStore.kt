package com.jarvis.launcher.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class HudColorTheme(
    val name: String,
    val primary: Long,
    val primaryDark: Long,
    val glow: Long
)

@Singleton
class PreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("aegis_prefs", Context.MODE_PRIVATE)

    private val _favoriteApps = MutableStateFlow(getFavoriteApps())
    val favoriteApps: StateFlow<List<String>> = _favoriteApps.asStateFlow()

    private val _colorThemeName = MutableStateFlow(getColorThemeName())
    val colorThemeName: StateFlow<String> = _colorThemeName.asStateFlow()

    fun getFavoriteApps(): List<String> {
        val raw = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        return raw.split(SEPARATOR).filter { it.isNotBlank() }
    }

    fun setFavoriteApps(packageNames: List<String>) {
        val trimmed = packageNames.take(MAX_FAVORITES)
        prefs.edit().putString(KEY_FAVORITES, trimmed.joinToString(SEPARATOR)).apply()
        _favoriteApps.value = trimmed
    }

    fun addFavoriteApp(packageName: String) {
        val current = getFavoriteApps().toMutableList()
        if (current.contains(packageName)) return
        if (current.size >= MAX_FAVORITES) current.removeAt(current.lastIndex)
        current.add(packageName)
        setFavoriteApps(current)
    }

    fun removeFavoriteApp(packageName: String) {
        setFavoriteApps(getFavoriteApps().filter { it != packageName })
    }

    fun getColorThemeName(): String {
        return prefs.getString(KEY_COLOR_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
    }

    fun setColorThemeName(name: String) {
        prefs.edit().putString(KEY_COLOR_THEME, name).apply()
        _colorThemeName.value = name
    }

    private val _voiceId = MutableStateFlow(getVoiceId())
    val voiceId: StateFlow<String> = _voiceId.asStateFlow()

    fun getVoiceId(): String = prefs.getString(KEY_VOICE, DEFAULT_VOICE) ?: DEFAULT_VOICE

    fun setVoiceId(id: String) {
        prefs.edit().putString(KEY_VOICE, id).apply()
        _voiceId.value = id
    }

    private val _alwaysListening = MutableStateFlow(getAlwaysListening())
    val alwaysListening: StateFlow<Boolean> = _alwaysListening.asStateFlow()

    fun getAlwaysListening(): Boolean = prefs.getBoolean(KEY_ALWAYS_LISTEN, false)

    fun setAlwaysListening(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALWAYS_LISTEN, enabled).apply()
        _alwaysListening.value = enabled
    }

    private val _useFahrenheit = MutableStateFlow(getUseFahrenheit())
    val useFahrenheit: StateFlow<Boolean> = _useFahrenheit.asStateFlow()

    fun getUseFahrenheit(): Boolean = prefs.getBoolean(KEY_TEMP_UNIT, true)

    fun setUseFahrenheit(fahrenheit: Boolean) {
        prefs.edit().putBoolean(KEY_TEMP_UNIT, fahrenheit).apply()
        _useFahrenheit.value = fahrenheit
    }

    companion object {
        private const val KEY_FAVORITES = "favorite_apps"
        private const val KEY_COLOR_THEME = "color_theme"
        private const val KEY_ALWAYS_LISTEN = "always_listening"
        private const val KEY_VOICE = "voice_id"
        private const val KEY_TEMP_UNIT = "use_fahrenheit"
        private const val DEFAULT_VOICE = "en-US-AndrewMultilingualNeural"
        private const val SEPARATOR = ","
        private const val DEFAULT_THEME = "Cyan"
        const val MAX_FAVORITES = 16

        val COLOR_THEMES = listOf(
            HudColorTheme("Cyan", 0xFF00E5FF, 0xFF0097A7, 0x4D00E5FF),
            HudColorTheme("Blue", 0xFF2196F3, 0xFF0D47A1, 0x4D2196F3),
            HudColorTheme("Red", 0xFFFF1744, 0xFFB71C1C, 0x4DFF1744),
            HudColorTheme("Green", 0xFF00E676, 0xFF1B5E20, 0x4D00E676),
            HudColorTheme("Purple", 0xFFD500F9, 0xFF6A1B9A, 0x4DD500F9),
            HudColorTheme("Orange", 0xFFFF9100, 0xFFE65100, 0x4DFF9100),
            HudColorTheme("Gold", 0xFFFFD740, 0xFFFF8F00, 0x4DFFD740),
            HudColorTheme("White", 0xFFFFFFFF, 0xFFBDBDBD, 0x4DFFFFFF),
            HudColorTheme("Pink", 0xFFFF4081, 0xFFC51162, 0x4DFF4081),
        )

        fun getThemeByName(name: String): HudColorTheme {
            return COLOR_THEMES.find { it.name == name } ?: COLOR_THEMES.first()
        }
    }
}
