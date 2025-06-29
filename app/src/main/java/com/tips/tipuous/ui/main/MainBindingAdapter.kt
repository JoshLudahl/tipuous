package com.tips.tipuous.ui.main

import androidx.databinding.BindingAdapter
import com.google.android.material.chip.Chip
import com.tips.tipuous.R

@BindingAdapter("percentage")
fun Chip.setChipBackground(isPercentSame: Boolean) {
    if (isPercentSame) {
        setChipBackgroundColorResource(R.color.colorPrimary)
        setTextColor(resources.getColor(R.color.colorSmokeDark, null))
    } else {
        setChipBackgroundColorResource(R.color.colorSmokeDark)
        setTextColor(resources.getColor(R.color.colorWhite, null))
    }
}
