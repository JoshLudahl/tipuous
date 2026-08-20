package com.tips.tipuous.ui.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tips.tipuous.domain.TipCalculator
import com.tips.tipuous.model.AdvancedSplit
import com.tips.tipuous.model.Item
import com.tips.tipuous.model.Percent
import com.tips.tipuous.model.Person
import com.tips.tipuous.model.RoundingMode
import com.tips.tipuous.model.TipCalculationResult
import com.tips.tipuous.model.TipMode
import com.tips.tipuous.utilities.Conversion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val tipCalculator = TipCalculator()
    private val settingsManager =
        com.tips.tipuous.data.AppSettingsManager
            .getInstance()

    val bill: StateFlow<Double> = savedStateHandle.getStateFlow("bill", 0.0)
    val tipPercentEnum: StateFlow<Percent> =
        savedStateHandle.getStateFlow(
            "tipPercentEnum",
            settingsManager.defaultTipPercent.value,
        )
    val customTipPercent: StateFlow<Int> = savedStateHandle.getStateFlow("customTipPercent", 20)
    val splitCount: StateFlow<Int> = savedStateHandle.getStateFlow("splitCount", 1)

    val taxAmount: StateFlow<Double> = savedStateHandle.getStateFlow("taxAmount", 0.0)
    val calculateTipOnPreTax: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(
            "calculateTipOnPreTax",
            initialValue = false,
        )
    val roundingMode: StateFlow<RoundingMode> =
        savedStateHandle.getStateFlow(
            "roundingMode",
            RoundingMode.NONE,
        )

    val tipMode: StateFlow<TipMode> = savedStateHandle.getStateFlow("tipMode", TipMode.PERCENT)
    val fixedTipAmount: StateFlow<Double> = savedStateHandle.getStateFlow("fixedTipAmount", 0.0)

    val isAdvancedMode: StateFlow<Boolean> = savedStateHandle.getStateFlow("isAdvancedMode", false)
    val advancedSplit: StateFlow<AdvancedSplit> =
        savedStateHandle.getStateFlow(
            "advancedSplit",
            AdvancedSplit(),
        )

    private data class CoreInputs(
        val bill: Double,
        val tipPercent: Percent,
        val customTip: Int,
        val split: Int,
        val tipMode: TipMode,
    )

    private data class ExtraInputs(
        val tax: Double,
        val preTax: Boolean,
        val rounding: RoundingMode,
        val fixedTip: Double,
        val isAdvanced: Boolean,
        val advancedSplit: AdvancedSplit,
    )

    // Core calculation result state - using nested combine to avoid vararg type inference issues
    val calculationResult: StateFlow<TipCalculationResult?> =
        combine(
            combine(bill, tipPercentEnum, customTipPercent, splitCount, tipMode) { b, t, c, s, m ->
                CoreInputs(b, t, c, s, m)
            },
            combine(
                combine(taxAmount, calculateTipOnPreTax, roundingMode) { tax, pre, round ->
                    Triple(tax, pre, round)
                },
                combine(fixedTipAmount, isAdvancedMode, advancedSplit) { fixed, advanced, data ->
                    Triple(fixed, advanced, data)
                },
            ) { group1, group2 ->
                ExtraInputs(
                    tax = group1.first,
                    preTax = group1.second,
                    rounding = group1.third,
                    fixedTip = group2.first,
                    isAdvanced = group2.second,
                    advancedSplit = group2.third,
                )
            },
        ) { core, extra ->
            tipCalculator.calculate(
                billAmount = core.bill,
                tipPercentEnum = core.tipPercent,
                customTipPercent = core.customTip,
                splitCount = core.split,
                taxAmount = extra.tax,
                calculateTipOnPreTax = extra.preTax,
                roundingMode = extra.rounding,
                tipMode = core.tipMode,
                fixedTipAmount = extra.fixedTip,
                advancedSplit = if (extra.isAdvanced) extra.advancedSplit else null,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    // Derived UI states
    val billAmountFormatted: StateFlow<String> =
        calculationResult
            .map { result ->
                if (result == null || result.billAmount == 0.0) {
                    "0.00"
                } else {
                    Conversion.formatNumberToIncludeTrailingZero(result.billAmount)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = "0.00",
            )

    val totalAmountFormatted: StateFlow<String> =
        calculationResult
            .map { result ->
                if (result == null || result.billAmount == 0.0) {
                    "-"
                } else {
                    Conversion.formatNumberToIncludeTrailingZero(result.totalAmount)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = "-",
            )

    val tipAmountFormatted: StateFlow<String> =
        calculationResult
            .map { result ->
                if (result == null || result.billAmount == 0.0) {
                    "0.00"
                } else {
                    Conversion.formatNumberToIncludeTrailingZero(result.tipAmount)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = "0.00",
            )

    val taxAmountFormatted: StateFlow<String> =
        calculationResult
            .map { result ->
                if (result == null || result.taxAmount == 0.0) {
                    "0.00"
                } else {
                    Conversion.formatNumberToIncludeTrailingZero(result.taxAmount)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = "0.00",
            )

    val amountPerPersonFormatted: StateFlow<String> =
        calculationResult
            .map { result ->
                if (result == null || result.billAmount == 0.0 || result.splitCount <= 1) {
                    "0.00"
                } else {
                    Conversion.formatNumberToIncludeTrailingZero(result.amountPerPerson)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = "0.00",
            )

    val isShareable: StateFlow<Boolean> =
        calculationResult
            .map { result ->
                result?.isShareable ?: false
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = false,
            )

    fun setBill(amount: Double) {
        savedStateHandle["bill"] = amount
    }

    fun setTipMode(mode: TipMode) {
        savedStateHandle["tipMode"] = mode
    }

    fun setFixedTipAmount(amount: Double) {
        savedStateHandle["fixedTipAmount"] = amount
    }

    fun updateTipPercentage(percent: Percent) {
        val current = tipPercentEnum.value
        savedStateHandle["tipPercentEnum"] = if (current == percent) Percent.NONE else percent
    }

    fun handleCustomPercentageClick() {
        val current = tipPercentEnum.value
        savedStateHandle["tipPercentEnum"] = if (current == Percent.CUSTOM) Percent.NONE else Percent.CUSTOM
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

    fun setAdvancedMode(enabled: Boolean) {
        savedStateHandle["isAdvancedMode"] = enabled
    }

    fun addPerson(name: String) {
        val current = advancedSplit.value
        val newPeople = current.people + Person(name = name)
        savedStateHandle["advancedSplit"] = current.copy(people = newPeople)

        // Ensure splitCount is at least the number of people
        if (splitCount.value < newPeople.size) {
            updateSplitCount(newPeople.size)
        }
    }

    fun removePerson(personId: String) {
        val current = advancedSplit.value
        val newPeople = current.people.filter { it.id != personId }
        savedStateHandle["advancedSplit"] = current.copy(people = newPeople)
    }

    fun addItemToPerson(
        personId: String,
        itemName: String,
        amount: Double,
    ) {
        val current = advancedSplit.value
        val newPeople =
            current.people.map { person ->
                if (person.id == personId) {
                    person.copy(items = person.items + Item(name = itemName, amount = amount))
                } else {
                    person
                }
            }
        savedStateHandle["advancedSplit"] = current.copy(people = newPeople)
    }

    fun removeItemFromPerson(
        personId: String,
        itemId: String,
    ) {
        val current = advancedSplit.value
        val newPeople =
            current.people.map { person ->
                if (person.id == personId) {
                    person.copy(items = person.items.filter { it.id != itemId })
                } else {
                    person
                }
            }
        savedStateHandle["advancedSplit"] = current.copy(people = newPeople)
    }

    fun formatBillWithTipForSharing(): String {
        val result = calculationResult.value ?: return "No calculation performed yet."
        if (!result.isShareable) return "Enter a bill amount to share."

        var splitDetails = ""
        if (result.splitCount > 1) {
            splitDetails = "Split (${result.splitCount} ways): \$${Conversion.formatNumberToIncludeTrailingZero(result.amountPerPerson)}/each"

            if (isAdvancedMode.value && result.personResults.isNotEmpty()) {
                val advancedDetails =
                    advancedSplit.value.people.joinToString("\n") { person ->
                        val total = result.personResults[person.id] ?: 0.0
                        "${person.name}: \$${Conversion.formatNumberToIncludeTrailingZero(total)}"
                    }
                splitDetails += "\nAdvanced Split:\n$advancedDetails"
            }
        }

        val taxInfo =
            if (result.taxAmount > 0.0) {
                "\nTax: \$${Conversion.formatNumberToIncludeTrailingZero(result.taxAmount)}" +
                    (if (result.isTipCalculatedOnPreTax) " (Tip calculated on subtotal)" else "")
            } else {
                ""
            }

        return "Bill: \$${Conversion.formatNumberToIncludeTrailingZero(result.billAmount)}$taxInfo\n" +
            "Tip: \$${Conversion.formatNumberToIncludeTrailingZero(result.tipAmount)}\n" +
            "Total: \$${Conversion.formatNumberToIncludeTrailingZero(result.totalAmount)}\n" +
            splitDetails.trim()
    }
}
