package com.tips.tipuous.domain

import com.tips.tipuous.model.Percent
import com.tips.tipuous.model.RoundingMode
import com.tips.tipuous.model.TipCalculationResult
import com.tips.tipuous.model.TipMode
import com.tips.tipuous.utilities.Conversion
import kotlin.math.ceil
import kotlin.math.floor

class TipCalculator {

    fun calculate(
        billAmount: Double,
        tipPercentEnum: Percent,
        customTipPercent: Int, // e.g., 25 for 25%
        splitCount: Int,
        taxAmount: Double = 0.0,
        calculateTipOnPreTax: Boolean = false,
        roundingMode: RoundingMode = RoundingMode.NONE,
        tipMode: TipMode = TipMode.PERCENT,
        fixedTipAmount: Double = 0.0,
    ): TipCalculationResult {
        val (tipAmount, tipPercentageValue) = if (tipMode == TipMode.PERCENT) {
            val percentageValue = when (tipPercentEnum) {
                Percent.FIFTEEN -> 0.15
                Percent.EIGHTEEN -> 0.18
                Percent.TWENTY -> 0.20
                Percent.CUSTOM -> customTipPercent / 100.0
                Percent.NONE -> 0.0
            }

            val baseAmountForTip = if (calculateTipOnPreTax) {
                billAmount
            } else {
                billAmount + taxAmount
            }

            Conversion.roundDoubleToTwoDecimalPlaces(baseAmountForTip * percentageValue) to percentageValue
        } else {
            val baseAmountForTip = if (calculateTipOnPreTax) {
                billAmount
            } else {
                billAmount + taxAmount
            }

            val effectivePercentage = if (baseAmountForTip > 0.0) {
                fixedTipAmount / baseAmountForTip
            } else {
                0.0
            }
            Conversion.roundDoubleToTwoDecimalPlaces(fixedTipAmount) to effectivePercentage
        }

        var totalAmount = Conversion.roundDoubleToTwoDecimalPlaces(billAmount + taxAmount + tipAmount)

        totalAmount = when (roundingMode) {
            RoundingMode.UP -> ceil(totalAmount)
            RoundingMode.DOWN -> floor(totalAmount)
            RoundingMode.NONE -> totalAmount
        }

        val amountPerPerson = if (splitCount > 0) {
            Conversion.roundDoubleToTwoDecimalPlaces(totalAmount / splitCount)
        } else {
            totalAmount
        }

        val isShareable = (billAmount > 0.0) && (totalAmount > 0.0)

        return TipCalculationResult(
            billAmount = billAmount,
            tipPercentageValue = tipPercentageValue,
            tipAmount = tipAmount,
            totalAmount = totalAmount,
            splitCount = splitCount,
            amountPerPerson = amountPerPerson,
            isShareable = isShareable,
            taxAmount = taxAmount,
            isTipCalculatedOnPreTax = calculateTipOnPreTax,
        )
    }
}
