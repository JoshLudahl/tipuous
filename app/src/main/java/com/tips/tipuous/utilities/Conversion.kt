package com.tips.tipuous.utilities

import android.util.Log
import kotlin.math.roundToInt

object Conversion {
    fun roundDoubleToTwoDecimalPlaces(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }

    fun formatNumberToIncludeTrailingZero(value: Double): String {
        Log.i("value", "$value")
        val string = value.toString().split('.')

        if(string[1].length == 1) {
            return "${string[0]}.${string[1]}0"
        }
        return "${string[0]}.${string[1]}"
    }
}
