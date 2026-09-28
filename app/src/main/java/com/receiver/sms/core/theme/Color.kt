package com.receiver.sms.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** The palette. Screens read colours through MaterialTheme or [StatusColors], never from here. */
internal object Palette {
    val blue40: Color = Color(0xFF2F63D8)
    val blue80: Color = Color(0xFFB2C5FF)
    val blue90: Color = Color(0xFFDAE2FF)
    val blue20: Color = Color(0xFF002C71)
    val slate40: Color = Color(0xFF575E71)
    val slate80: Color = Color(0xFFBFC6DC)
    val teal40: Color = Color(0xFF006A62)
    val teal80: Color = Color(0xFF6FD8CB)
    val surfaceLight: Color = Color(0xFFF9F8FE)
    val surfaceDark: Color = Color(0xFF121316)
    // Success/failure pair validated for colour-vision deficiency on both surfaces (teal vs orange, not green vs red).
    val teal: Color = Color(0xFF00897B)
    val tealDark: Color = Color(0xFF26A69A)
    val orange: Color = Color(0xFFE8710A)
    val orangeDark: Color = Color(0xFFD2691E)
}

internal val LightColors: ColorScheme = lightColorScheme(
    primary = Palette.blue40,
    onPrimary = Color.White,
    primaryContainer = Palette.blue90,
    onPrimaryContainer = Palette.blue20,
    secondary = Palette.slate40,
    tertiary = Palette.teal40,
    background = Palette.surfaceLight,
    surface = Palette.surfaceLight,
)

internal val DarkColors: ColorScheme = darkColorScheme(
    primary = Palette.blue80,
    onPrimary = Palette.blue20,
    secondary = Palette.slate80,
    tertiary = Palette.teal80,
    background = Palette.surfaceDark,
    surface = Palette.surfaceDark,
)

/** Semantic colours Material has no role for. */
data class StatusColors(val success: Color, val failure: Color)

internal val LightStatusColors: StatusColors = StatusColors(Palette.teal, Palette.orange)
internal val DarkStatusColors: StatusColors = StatusColors(Palette.tealDark, Palette.orangeDark)
