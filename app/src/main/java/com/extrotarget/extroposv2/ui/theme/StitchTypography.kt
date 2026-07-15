package com.extrotarget.extroposv2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Note: Inter font family should be added to res/font in a real implementation.
// For now, we use the default SansSerif which maps to system Inter on many modern devices.
val Inter = FontFamily.SansSerif

val StitchTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Inter,
        fontSize = 32.sp,
        fontWeight = FontWeight.Black,
        lineHeight = 40.sp,
        letterSpacing = (-0.03).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 24.sp,
        letterSpacing = (-0.01).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp
    )
)

// Extension properties for semantic mapping
val Typography.priceLarge: TextStyle get() = displayLarge
val Typography.titleMedium: TextStyle get() = headlineMedium
val Typography.bodyBase: TextStyle get() = bodyLarge
val Typography.labelCaps: TextStyle get() = labelSmall
