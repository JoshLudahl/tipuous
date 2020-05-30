package com.tips.tipuous

data class Tips(
    var billAmount: Double = 0.0,
    var tipAmount: Double = 0.0,
    var billTotal: Double = 0.0,
    var tipAmountFormatted: String = "$$tipAmount",
    var totalAmountFormatted: String = "$$billTotal"
)