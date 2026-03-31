package com.sobhaty.model

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.sobhaty.R

data class Dhikr(val id: Int, val name: String, val fullText: String, val defaultTarget: Int)

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val ArabicFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Amiri"), fontProvider = provider)
)
