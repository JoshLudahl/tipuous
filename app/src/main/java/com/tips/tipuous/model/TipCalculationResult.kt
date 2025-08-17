package com.tips.tipuous.model

data class TipCalculationResult(
    val billAmount: Double,
    val tipPercentageValue: Double, // e.g., 0.10 for 10%, 0.25 for 25%
    val tipAmount: Double,
    val totalAmount: Double,
    val splitCount: Int,
    val amountPerPerson: Double,
    val isShareable: Boolean
)
