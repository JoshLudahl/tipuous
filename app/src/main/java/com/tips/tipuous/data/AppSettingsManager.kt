package com.tips.tipuous.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.tips.tipuous.model.Percent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettingsManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _defaultTipPercent =
        MutableStateFlow(
            Percent.entries.getOrElse(prefs.getInt("default_tip_percent", Percent.EIGHTEEN.ordinal)) { Percent.EIGHTEEN },
        )
    val defaultTipPercent: StateFlow<Percent> = _defaultTipPercent.asStateFlow()

    companion object {
        @Volatile
        private var instance: AppSettingsManager? = null

        fun getInstance(context: Context): AppSettingsManager =
            instance ?: synchronized(this) {
                instance ?: AppSettingsManager(context.applicationContext).also { instance = it }
            }

        fun getInstance(): AppSettingsManager = instance ?: throw IllegalStateException("AppSettingsManager not initialized")
    }

    fun setDefaultTipPercent(percent: Percent) {
        _defaultTipPercent.value = percent
        prefs.edit { putInt("default_tip_percent", percent.ordinal) }
    }
}
