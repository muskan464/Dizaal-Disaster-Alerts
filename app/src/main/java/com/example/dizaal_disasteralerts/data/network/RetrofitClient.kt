package com.example.dizaal_disasteralerts.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://flood-api.open-meteo.com/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val floodApiService: FloodApiService = retrofit.create(FloodApiService::class.java)
}
