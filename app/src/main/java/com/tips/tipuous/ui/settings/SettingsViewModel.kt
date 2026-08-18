package com.tips.tipuous.ui.settings

import androidx.lifecycle.ViewModel
import com.tips.tipuous.data.AppSettingsManager
import com.tips.tipuous.model.Percent
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {
    private val settingsManager = AppSettingsManager.getInstance()

    val defaultTipPercent: StateFlow<Percent> = settingsManager.defaultTipPercent

    fun setDefaultTipPercent(percent: Percent) {
        settingsManager.setDefaultTipPercent(percent)
    }
}
