// file: data/network/RetrofitClient.kt
package com.example.dizaal_disasteralerts.data.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Flood API (existing)
    private const val FLOOD_BASE_URL = "https://flood-api.open-meteo.com/"

    // USGS Earthquake API
    private const val USGS_BASE_URL = "https://earthquake.usgs.gov/"

    private val gson = GsonBuilder().create()

    private val okHttp = OkHttpClient.Builder().apply {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        addInterceptor(logging)
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
    }.build()

    // Flood retrofit (existing)
    val floodRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(FLOOD_BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val floodApiService: com.example.dizaal_disasteralerts.data.network.FloodApiService =
        floodRetrofit.create(com.example.dizaal_disasteralerts.data.network.FloodApiService::class.java)

    // USGS retrofit (new)
    private val usgsRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(USGS_BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val earthquakeApiService: com.example.dizaal_disasteralerts.data.network.EarthquakeApiService =
        usgsRetrofit.create(com.example.dizaal_disasteralerts.data.network.EarthquakeApiService::class.java)
}
