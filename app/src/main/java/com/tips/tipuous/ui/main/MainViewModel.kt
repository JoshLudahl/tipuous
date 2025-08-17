package com.tips.tipuous.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tips.tipuous.domain.TipCalculator
import com.tips.tipuous.model.Percent
import com.tips.tipuous.model.TipCalculationResult
import com.tips.tipuous.utilities.Conversion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val tipCalculator = TipCalculator() // For simplicity, direct instantiation. Consider Hilt/Dagger for DI.

    private val _bill = MutableStateFlow(0.00)
    val bill: StateFlow<Double> get() = _bill

    private val _tipPercentEnum = MutableStateFlow(Percent.TEN)
    val tipPercentEnum: StateFlow<Percent> get() = _tipPercentEnum

    private val _customTipPercent = MutableStateFlow(20) // Represents the whole number, e.g., 20 for 20%
    val customTipPercent: StateFlow<Int> get() = _customTipPercent

    private val _splitCount = MutableStateFlow(1) // Renamed from _split for clarity, assuming Int
    val splitCount: StateFlow<Int> get() = _splitCount

    // Core calculation result state
    private val _calculationResult = MutableStateFlow<TipCalculationResult?>(null)
    val calculationResult: StateFlow<TipCalculationResult?> get() = _calculationResult

    // Derived UI states
    val totalAmountFormatted: StateFlow<String> = calculationResult.map { result ->
        if (result == null || result.billAmount == 0.0) "-"
        else Conversion.formatNumberToIncludeTrailingZero(result.totalAmount)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "-")

    val tipAmountFormatted: StateFlow<String> = calculationResult.map { result ->
        if (result == null || result.billAmount == 0.0) "0.00"
        else Conversion.formatNumberToIncludeTrailingZero(result.tipAmount)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "0.00")

    val amountPerPersonFormatted: StateFlow<String> = calculationResult.map { result ->
        if (result == null || result.billAmount == 0.0 || result.splitCount <= 1) "0.00" // Hide if not splitting or no bill
        else Conversion.formatNumberToIncludeTrailingZero(result.amountPerPerson)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "0.00")

    val isShareable: StateFlow<Boolean> = calculationResult.map { result ->
        result?.isShareable ?: false
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        // Recalculate whenever an input changes
        viewModelScope.launch {
            combine(
                _bill,
                _tipPercentEnum,
                _customTipPercent,
                _splitCount
            ) { bill, tipEnum, customPercent, split ->
                performCalculation()
            }.collect {}
        }
        performCalculation()
    }

    private fun performCalculation() {
        _calculationResult.value = tipCalculator.calculate(
            billAmount = _bill.value,
            tipPercentEnum = _tipPercentEnum.value,
            customTipPercent = _customTipPercent.value,
            splitCount = _splitCount.value
        )
    }

    fun setBill(amount: Double) {
        _bill.value = amount
    }

    fun updateTipPercentage(percent: Percent) {
        _tipPercentEnum.value = percent
    }

    fun handleCustomPercentageClick() {
        _tipPercentEnum.value = Percent.CUSTOM

    }

    fun updateCustomTipValue(value: Int) {
        _customTipPercent.value = value
    }

    fun updateSplitCount(count: Int) {
        _splitCount.value = if (count > 0) count else 1
    }

    fun formatBillWithTipForSharing(): String {
        val result = _calculationResult.value ?: return "No calculation performed yet."
        if (!result.isShareable) return "Enter a bill amount to share."

        var splitDetails = ""
        if (result.splitCount > 1) {
            splitDetails = "Split (${result.splitCount} ways): ${'$'}${Conversion.formatNumberToIncludeTrailingZero(result.amountPerPerson)}/each"
        }

        return """
            Bill: ${'$'}${Conversion.formatNumberToIncludeTrailingZero(result.billAmount)}
            Tip: ${'$'}${Conversion.formatNumberToIncludeTrailingZero(result.tipAmount)}
            Total: ${'$'}${Conversion.formatNumberToIncludeTrailingZero(result.totalAmount)}
            ${splitDetails.trim()}
        """.trimIndent()
    }
}
