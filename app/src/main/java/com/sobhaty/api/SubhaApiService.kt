package com.sobhaty.api

import com.sobhaty.model.AthkarResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface SubhaApiService {
    @GET("athkar.json")
    suspend fun getAthkar(): Response<AthkarResponse> // تغيير الاستجابة لتكون كائن الـ Response الكامل

    companion object {
        private const val BASE_URL = "https://sobhaty-ef1b4-default-rtdb.europe-west1.firebasedatabase.app/"

        fun create(): SubhaApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubhaApiService::class.java)
        }
    }
}
