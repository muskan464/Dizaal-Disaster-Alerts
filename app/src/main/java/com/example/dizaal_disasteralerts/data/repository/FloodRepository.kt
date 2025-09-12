package com.example.dizaal_disasteralerts.data.repository

import com.example.dizaal_disasteralerts.data.model.FloodResponse
import com.example.dizaal_disasteralerts.data.network.RetrofitClient
import retrofit2.Call

class FloodRepository {
    private val api = RetrofitClient.floodApiService

    fun getFloodData(lat: Double, lon: Double): Call<FloodResponse> {
        return api.getFloodData(lat, lon)
    }
}
