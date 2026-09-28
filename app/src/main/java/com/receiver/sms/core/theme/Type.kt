package com.receiver.sms.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.receiver.sms.R

private val OpenSans: FontFamily = FontFamily(
    Font(R.font.open_sans_light_300, FontWeight.Light),
    Font(R.font.open_sans_regular_400, FontWeight.Normal),
    Font(R.font.open_sans_medium_500, FontWeight.Medium),
    Font(R.font.open_sans_semi_bold_600, FontWeight.SemiBold),
    Font(R.font.open_sans_bold_700, FontWeight.Bold),
    Font(R.font.open_sans_extra_bold_800, FontWeight.ExtraBold),
)

private fun TextStyle.openSans(): TextStyle = copy(fontFamily = OpenSans)

internal val AppTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.openSans(),
        displayMedium = displayMedium.openSans(),
        displaySmall = displaySmall.openSans(),
        headlineLarge = headlineLarge.openSans(),
        headlineMedium = headlineMedium.openSans(),
        headlineSmall = headlineSmall.openSans().copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.openSans().copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.openSans().copy(fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.openSans(),
        bodyLarge = bodyLarge.openSans(),
        bodyMedium = bodyMedium.openSans(),
        bodySmall = bodySmall.openSans(),
        labelLarge = labelLarge.openSans(),
        labelMedium = labelMedium.openSans(),
        labelSmall = labelSmall.openSans(),
    )
}

/** For request/response bodies and URLs. */
val CodeFontFamily: FontFamily = FontFamily.Monospace
