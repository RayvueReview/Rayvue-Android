package com.bigbratan.rayvue.ui.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bigbratan.rayvue.R

val plusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_semi_bold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
)

val rubikMonoOne = FontFamily(
    Font(R.font.rubik_mono_one_regular),
)

val noFontPadding = PlatformTextStyle(
    includeFontPadding = false,
)