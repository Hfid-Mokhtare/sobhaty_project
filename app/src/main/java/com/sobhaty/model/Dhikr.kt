package com.sobhaty.model

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.sobhaty.R
import com.google.gson.annotations.SerializedName

// موديل الذكر الشامل
data class Thikr(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: String,
    @SerializedName("text") val text: String,
    @SerializedName("count") val count: Int,
    @SerializedName("title") val title: String? = null,
    @SerializedName("verses") val verses: List<Verse>? = null,
    @SerializedName("hadith") val hadith: Hadith? = null
)

data class Verse(
    @SerializedName("text") val text: String,
    @SerializedName("surah") val surah: String,
    @SerializedName("ayah_number") val ayahNumber: String
)

data class Hadith(
    @SerializedName("text") val text: String,
    @SerializedName("source") val source: String,
    @SerializedName("reward") val reward: String? = null,
    @SerializedName("extra_info") val extraInfo: String? = null
)

// إعدادات الخط العثماني
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val ArabicFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Amiri"), fontProvider = provider)
)
