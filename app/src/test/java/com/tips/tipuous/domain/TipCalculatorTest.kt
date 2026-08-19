package com.tips.tipuous.domain

import com.tips.tipuous.model.Percent
import com.tips.tipuous.model.TipMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TipCalculatorTest {
    private val calculator = TipCalculator()

    @Test
    fun `calculate tip on 100 with 20 percent results in 120 total`() {
        val result =
            calculator.calculate(
                billAmount = 100.0,
                tipPercentEnum = Percent.TWENTY,
                customTipPercent = 20,
                splitCount = 1,
            )
        assertEquals(20.0, result.tipAmount, 0.01)
        assertEquals(120.0, result.totalAmount, 0.01)
    }

    @Test
    fun `calculate tip with fixed amount results in correct total`() {
        val result =
            calculator.calculate(
                billAmount = 100.0,
                tipPercentEnum = Percent.NONE,
                customTipPercent = 0,
                splitCount = 1,
                tipMode = TipMode.AMOUNT,
                fixedTipAmount = 25.0,
            )
        assertEquals(25.0, result.tipAmount, 0.01)
        assertEquals(125.0, result.totalAmount, 0.01)
        assertEquals(0.25, result.tipPercentageValue, 0.01)
    }

    @Test
    fun `adding 10 tax increases the total by at least 10`() {
        // Base case: 100 bill, 20% tip, 0 tax -> 120 total
        val baseResult =
            calculator.calculate(
                billAmount = 100.0,
                tipPercentEnum = Percent.TWENTY,
                customTipPercent = 20,
                splitCount = 1,
                taxAmount = 0.0,
            )

        // Tax case: 100 bill, 20% tip, 10 tax, tip on pre-tax -> 130 total
        val taxResultPreTax =
            calculator.calculate(
                billAmount = 100.0,
                tipPercentEnum = Percent.TWENTY,
                customTipPercent = 20,
                splitCount = 1,
                taxAmount = 10.0,
                calculateTipOnPreTax = true,
            )

        assertEquals(20.0, taxResultPreTax.tipAmount, 0.01)
        assertEquals(130.0, taxResultPreTax.totalAmount, 0.01)
        assertEquals(baseResult.totalAmount + 10.0, taxResultPreTax.totalAmount, 0.01)

        // Tax case: 100 bill, 20% tip, 10 tax, tip on total -> 132 total
        val taxResultOnTotal =
            calculator.calculate(
                billAmount = 100.0,
                tipPercentEnum = Percent.TWENTY,
                customTipPercent = 20,
                splitCount = 1,
                taxAmount = 10.0,
                calculateTipOnPreTax = false,
            )

        assertEquals(22.0, taxResultOnTotal.tipAmount, 0.01)
        assertEquals(132.0, taxResultOnTotal.totalAmount, 0.01)
    }

    @Test
    fun `toggling pre-tax switch correctly changes tip amount`() {
        val bill = 100.0
        val tax = 10.0

        // Pre-tax ON: tip = 100 * 0.2 = 20
        val preTaxOn =
            calculator.calculate(
                billAmount = bill,
                tipPercentEnum = Percent.TWENTY,
                customTipPercent = 20,
                splitCount = 1,
                taxAmount = tax,
                calculateTipOnPreTax = true,
            )
        assertEquals(20.0, preTaxOn.tipAmount, 0.01)

        // Pre-tax OFF: tip = (100 + 10) * 0.2 = 22
        val preTaxOff =
            calculator.calculate(
                billAmount = bill,
                tipPercentEnum = Percent.TWENTY,
                customTipPercent = 20,
                splitCount = 1,
                taxAmount = tax,
                calculateTipOnPreTax = false,
            )
        assertEquals(22.0, preTaxOff.tipAmount, 0.01)
    }
}
