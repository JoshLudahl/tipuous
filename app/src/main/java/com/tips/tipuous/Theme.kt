package com.tips.tipuous

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.ui.graphics.Color
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color.Companion.Black

private val lightGreen = Color(red = 204, green = 255, blue = 189)
private val darkGreen = Color(red = 126, green = 202, blue = 156)
private val darkGray = Color(red = 64, green = 57, blue = 74)
private val blackish = Color(red = 28, green = 20, blue = 39)

private val DarkColorPalette = darkColors(
    surface = blackish,
    primary = darkGreen,

)

private val LightColorPalette = lightColors(
    surface = blackish,
    onSurface = lightGreen
)

@Composable
fun BasicsCodelabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
