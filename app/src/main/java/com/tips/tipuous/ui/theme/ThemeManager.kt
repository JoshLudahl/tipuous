package com.tips.tipuous.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

class ThemeManager private constructor(
    context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode =
        MutableStateFlow(
            ThemeMode.entries[prefs.getInt("theme_mode", ThemeMode.SYSTEM.ordinal)],
        )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _dynamicColor =
        MutableStateFlow(
            prefs.getBoolean("dynamic_color", true),
        )
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    companion object {
        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager =
            instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }

        fun getInstance(): ThemeManager = instance ?: throw IllegalStateException("ThemeManager not initialized")
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit { putInt("theme_mode", mode.ordinal) }
    }

    fun setDynamicColor(enabled: Boolean) {
        _dynamicColor.value = enabled
        prefs.edit { putBoolean("dynamic_color", enabled) }
    }

    @Composable
    fun isDarkTheme(): Boolean {
        val mode by themeMode.collectAsState()
        return when (mode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    }
}
