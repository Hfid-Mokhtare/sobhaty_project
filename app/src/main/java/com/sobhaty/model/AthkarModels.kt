package com.sobhaty.model

import com.google.gson.annotations.SerializedName

// نكتفي هنا بتعريف الـ Response فقط لأن باقي الكلاسات معرفة في Dhikr.kt
data class AthkarResponse(
    @SerializedName("athkar")
    val athkar: List<Thikr>
)
