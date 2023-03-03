package com.jetpack.barcodescanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
//    primary = Purple200,
//    primaryVariant = Purple700,
//    secondary = Teal200

//    primary = Yellow200,
//    secondary = Blue200,

    primary = Indigo200,
    primaryVariant = Indigo800,
    secondary = Light_blue200,
    onSecondary = Black,
    onPrimary = Black,
    secondaryVariant = Light_blue200,
    onBackground = White,
    onSurface = White,
    background = Black,
    surface = Black,
    error = Red400
)

private val LightColorPalette = lightColors(
//    primary = Purple500,
//    primaryVariant = Purple700,
//    secondary = Teal200

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */

    primary = Indigo500,
    primaryVariant = Indigo800,
    secondary = Light_blue200,
    onSecondary = Black,
    onPrimary = White,
    secondaryVariant = Light_blue700,
    onBackground = Black,
    onSurface = Black,
    background = White,
    surface = White,
    error = Red700
)

@Composable
fun BarcodeScannerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}