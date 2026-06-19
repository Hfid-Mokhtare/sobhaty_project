package com.sobhaty.model

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.sobhaty.R
import com.google.gson.annotations.SerializedName

// الكائن الرئيسي الذي يحتوي على القائمة كما في الـ JSON
data class AthkarResponse(
    @SerializedName("athkar")
    val athkar: List<Thikr>
)

data class Thikr(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: String,
    @SerializedName("text") val text: String,
    @SerializedName("count") val count: Int,
    @SerializedName("verses") val verses: List<Verse>? = null,
    @SerializedName("hadith") val hadith: Hadith? = null
)

data class Verse(
    @SerializedName("text") val text: String,
    @SerializedName("surah") val surah: String,
    @SerializedName("ayah_number") val ayahNumber: Any? = null // استخدم Any لأن الرقم قد يكون string أو int
) {
    val displayAyahNumber: String
        get() = ayahNumber?.toString() ?: ""
}

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
    Font(
        googleFont = GoogleFont("Amiri"),
        fontProvider = provider
    )
)
