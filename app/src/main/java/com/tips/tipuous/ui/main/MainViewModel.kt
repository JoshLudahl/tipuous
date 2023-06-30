package com.tips.tipuous.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tips.tipuous.model.Percent
import com.tips.tipuous.utilities.Conversion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _bill = MutableStateFlow(0.00)
    val bill: StateFlow<Double>
        get() = _bill

    private val _tip = MutableStateFlow(Percent.TEN)
    val tip: StateFlow<Percent>
        get() = _tip

    private val _total = MutableStateFlow("-")
    val total: StateFlow<String>
        get() = _total

    private val _totalFormatted = MutableStateFlow("-")
    val totalFormatted: StateFlow<String>
        get() = _totalFormatted

    private val _customTip = MutableStateFlow(25)
    val customTip: StateFlow<Int>
        get() = _customTip

    private val _customTipLabel = MutableStateFlow("Custom tip (${customTip.value / 100}%)")
    val customTipLabel: StateFlow<String>
        get() = _customTipLabel

    private val _tipValue = MutableStateFlow("0.00")
    val tipValue: StateFlow<String>
        get() = _tipValue


    init {
        viewModelScope.launch {
            _customTip.collect {
                _customTipLabel.value = "Other: $it%"
            }
        }
    }

    fun calculateTip() {
        val tip = _bill.value * when (_tip.value) {
            Percent.TEN -> .10
            Percent.FIFTEEN -> .15
            Percent.TWENTY -> .20
            Percent.CUSTOM -> _customTip.value / 100.00
        }

        val value = Conversion.roundDoubleToTwoDecimalPlaces(_bill.value + tip)
        Log.i("bill", "${bill.value}")
        if (value == 0.0 || bill.value == 0.00 || bill.value.toString().isEmpty()) {
            _total.value = "-"
        } else {
            try {
                val tipAmount = Conversion.roundDoubleToTwoDecimalPlaces(tip)
                val tipWithTrailingZero = Conversion.formatNumberToIncludeTrailingZero(tipAmount)
                _tipValue.value = tipWithTrailingZero


            } catch (e: Exception) {
                Log.e("Error: ", "Error when converting string to double. \n$e")
            }
            _total.value = Conversion.formatNumberToIncludeTrailingZero(value)
        }
    }

    fun setBill(double: Double) {
        _bill.value = double
    }

    fun updateTipPercentage(percent: Percent) {
        _tip.value = percent
        calculateTip()
    }

    fun handleCustomPercentageClick() {
        _tip.value = Percent.CUSTOM
        calculateTip()
    }

    fun updateCustomValue(value: Int) {
        _customTip.value = value
    }

    fun updateTotal(value: String) {
        _total.value = value
    }

    fun clearValues() {
        _bill.value = 0.00
        _tipValue.value = "0.00"
    }
}
