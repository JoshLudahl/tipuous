package com.tips.tipuous.domain

import com.tips.tipuous.model.Percent
import com.tips.tipuous.model.TipCalculationResult
import com.tips.tipuous.utilities.Conversion

class TipCalculator {

    fun calculate(
        billAmount: Double,
        tipPercentEnum: Percent,
        customTipPercent: Int, // e.g., 25 for 25%
        splitCount: Int
    ): TipCalculationResult {
        val tipPercentageValue = when (tipPercentEnum) {
            Percent.FIVE -> 0.05
            Percent.TEN -> 0.10
            Percent.FIFTEEN -> 0.15
            Percent.TWENTY -> 0.20
            Percent.CUSTOM -> customTipPercent / 100.0
        }

        val tipAmount = Conversion.roundDoubleToTwoDecimalPlaces(billAmount * tipPercentageValue)
        val totalAmount = Conversion.roundDoubleToTwoDecimalPlaces(billAmount + tipAmount)
        val amountPerPerson = if (splitCount > 0) {
            Conversion.roundDoubleToTwoDecimalPlaces(totalAmount / splitCount)
        } else {
            totalAmount // or handle error/edge case as appropriate
        }

        val isShareable = billAmount > 0.0 && totalAmount > 0.0

        return TipCalculationResult(
            billAmount = billAmount,
            tipPercentageValue = tipPercentageValue,
            tipAmount = tipAmount,
            totalAmount = totalAmount,
            splitCount = splitCount,
            amountPerPerson = amountPerPerson,
            isShareable = isShareable
        )
    }
}
