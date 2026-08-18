package com.tips.tipuous.ui.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tips.tipuous.domain.TipCalculator
import com.tips.tipuous.model.Percent
import com.tips.tipuous.model.RoundingMode
import com.tips.tipuous.model.TipCalculationResult
import com.tips.tipuous.utilities.Conversion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val tipCalculator = TipCalculator()
    private val settingsManager = com.tips.tipuous.data.AppSettingsManager.getInstance()

    val bill: StateFlow<Double> = savedStateHandle.getStateFlow("bill", 0.0)
    val tipPercentEnum: StateFlow<Percent> = savedStateHandle.getStateFlow("tipPercentEnum", settingsManager.defaultTipPercent.value)
    val customTipPercent: StateFlow<Int> = savedStateHandle.getStateFlow("customTipPercent", 20)
    val splitCount: StateFlow<Int> = savedStateHandle.getStateFlow("splitCount", 1)
    
    val taxAmount: StateFlow<Double> = savedStateHandle.getStateFlow("taxAmount", 0.0)
    val calculateTipOnPreTax: StateFlow<Boolean> = savedStateHandle.getStateFlow("calculateTipOnPreTax", false)
    val roundingMode: StateFlow<RoundingMode> = savedStateHandle.getStateFlow("roundingMode", RoundingMode.NONE)

    private data class CoreInputs(val bill: Double, val tipPercent: Percent, val customTip: Int, val split: Int)
    private data class ExtraInputs(val tax: Double, val preTax: Boolean, val rounding: RoundingMode)

    // Core calculation result state - using nested combine to avoid vararg type inference issues
    val calculationResult: StateFlow<TipCalculationResult?> = combine(
        combine(bill, tipPercentEnum, customTipPercent, splitCount) { b, t, c, s -> CoreInputs(b, t, c, s) },
        combine(taxAmount, calculateTipOnPreTax, roundingMode) { tax, pre, round -> ExtraInputs(tax, pre, round) }
    ) { core, extra ->
        tipCalculator.calculate(
            billAmount = core.bill,
            tipPercentEnum = core.tipPercent,
            customTipPercent = core.customTip,
            splitCount = core.split,
            taxAmount = extra.tax,
            calculateTipOnPreTax = extra.preTax,
            roundingMode = extra.rounding
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

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
        if (result == null || result.billAmount == 0.0 || result.splitCount <= 1) "0.00"
        else Conversion.formatNumberToIncludeTrailingZero(result.amountPerPerson)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "0.00")

    val isShareable: StateFlow<Boolean> = calculationResult.map { result ->
        result?.isShareable ?: false
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setBill(amount: Double) {
        savedStateHandle["bill"] = amount
    }

    fun updateTipPercentage(percent: Percent) {
        savedStateHandle["tipPercentEnum"] = percent
    }

    fun handleCustomPercentageClick() {
        savedStateHandle["tipPercentEnum"] = Percent.CUSTOM
    }

    fun updateCustomTipValue(value: Int) {
        savedStateHandle["customTipPercent"] = value
    }

    fun updateSplitCount(count: Int) {
        savedStateHandle["splitCount"] = if (count > 0) count else 1
    }

    fun setTaxAmount(amount: Double) {
        savedStateHandle["taxAmount"] = amount
    }

    fun setCalculateTipOnPreTax(enabled: Boolean) {
        savedStateHandle["calculateTipOnPreTax"] = enabled
    }

    fun setRoundingMode(mode: RoundingMode) {
        val current = roundingMode.value
        savedStateHandle["roundingMode"] = if (current == mode) RoundingMode.NONE else mode
    }

    fun formatBillWithTipForSharing(): String {
        val result = calculationResult.value ?: return "No calculation performed yet."
        if (!result.isShareable) return "Enter a bill amount to share."

        var splitDetails = ""
        if (result.splitCount > 1) {
            splitDetails = "Split (${result.splitCount} ways): ${'$'}${Conversion.formatNumberToIncludeTrailingZero(result.amountPerPerson)}/each"
        }

        val taxInfo = if (result.taxAmount > 0.0) {
            "\nTax: ${'$'}${Conversion.formatNumberToIncludeTrailingZero(result.taxAmount)}" +
            if (result.isTipCalculatedOnPreTax) " (Tip calculated on subtotal)" else ""
        } else ""

        return """
            Bill: ${'$'}${Conversion.formatNumberToIncludeTrailingZero(result.billAmount)}$taxInfo
            Tip: ${'$'}${Conversion.formatNumberToIncludeTrailingZero(result.tipAmount)}
            Total: ${'$'}${Conversion.formatNumberToIncludeTrailingZero(result.totalAmount)}
            ${splitDetails.trim()}
        """.trimIndent()
    }
}
