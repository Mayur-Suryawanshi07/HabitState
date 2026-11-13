package com.mayur.habitstate.data.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class ThemePreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("habitstate_theme_prefs", Context.MODE_PRIVATE)

    private val _themeModeState = MutableStateFlow(getThemeMode())
    val themeModeState: StateFlow<ThemeMode> = _themeModeState.asStateFlow()

    fun getThemeMode(): ThemeMode {
        val raw = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeModeState.value = mode
    }

    fun toggleDarkMode(enableDark: Boolean) {
        val newMode = if (enableDark) ThemeMode.DARK else ThemeMode.LIGHT
        setThemeMode(newMode)
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"

        @Volatile
        private var INSTANCE: ThemePreferencesRepository? = null

        fun getInstance(context: Context): ThemePreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemePreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
