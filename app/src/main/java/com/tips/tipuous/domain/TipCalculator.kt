package com.tips.tipuous.domain

import com.tips.tipuous.model.AdvancedSplit
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
        advancedSplit: AdvancedSplit? = null,
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

        // Advanced Split Logic
        val personResults = mutableMapOf<String, Double>()
        val advancedPeople = advancedSplit?.people ?: emptyList()

        if (advancedPeople.isNotEmpty()) {
            val totalBillSubtotal = if (billAmount > 0) billAmount else 1.0

            // Calculate individual subtotals for advanced people
            val advancedSubtotals = advancedPeople.map { person ->
                person.id to person.items.sumOf { it.amount }
            }.toMap()

            val totalAdvancedSubtotal = advancedSubtotals.values.sum()
            val sharedSubtotal = (billAmount - totalAdvancedSubtotal).coerceAtLeast(0.0)

            val totalPeopleCount = splitCount.coerceAtLeast(advancedPeople.size)
            val sharedPeopleCount = (totalPeopleCount - advancedPeople.size).coerceAtLeast(0)

            val individualSharedSubtotal = if (sharedPeopleCount > 0) {
                sharedSubtotal / sharedPeopleCount
            } else {
                0.0
            }

            // Calculate total for each advanced person
            advancedPeople.forEach { person ->
                val personSubtotal = advancedSubtotals[person.id] ?: 0.0
                val shareRatio = personSubtotal / totalBillSubtotal

                // Add proportional tax and tip
                // Note: We use the pre-rounded totalAmount to distribute, but it might be better to distribute tax and tip separately
                // so the sum of individual totals equals the grand total.
                val personTax = taxAmount * shareRatio
                val personTip = tipAmount * shareRatio

                val personTotal = personSubtotal + personTax + personTip

                // If the grand total was rounded, we should probably round the individual totals too or adjust them.
                // For simplicity, we'll keep them as is and let the UI format them.
                personResults[person.id] = Conversion.roundDoubleToTwoDecimalPlaces(personTotal)
            }

            // The "amountPerPerson" in the result will represent the shared person's total
            val sharedShareRatio = individualSharedSubtotal / totalBillSubtotal
            val sharedTax = taxAmount * sharedShareRatio
            val sharedTip = tipAmount * sharedShareRatio
            val sharedTotal = individualSharedSubtotal + sharedTax + sharedTip

            val amountPerPerson = Conversion.roundDoubleToTwoDecimalPlaces(sharedTotal)

            return TipCalculationResult(
                billAmount = billAmount,
                tipPercentageValue = tipPercentageValue,
                tipAmount = tipAmount,
                totalAmount = totalAmount,
                splitCount = totalPeopleCount,
                amountPerPerson = amountPerPerson,
                isShareable = billAmount > 0.0 && totalAmount > 0.0,
                taxAmount = taxAmount,
                isTipCalculatedOnPreTax = calculateTipOnPreTax,
                personResults = personResults
            )
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
