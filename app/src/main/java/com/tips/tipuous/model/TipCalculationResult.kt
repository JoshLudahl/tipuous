package com.tips.tipuous.model

data class TipCalculationResult(
    val billAmount: Double,
    val tipPercentageValue: Double,
    val tipAmount: Double,
    val totalAmount: Double,
    val splitCount: Int,
    val amountPerPerson: Double,
    val isShareable: Boolean,
    val taxAmount: Double = 0.0,
    val isTipCalculatedOnPreTax: Boolean = false,
    val personResults: Map<String, Double> = emptyMap(),
)

enum class RoundingMode {
    NONE,
    UP,
    DOWN,
}
