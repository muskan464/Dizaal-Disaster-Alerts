// file: data/repository/EarthquakeRepository.kt
package com.example.dizaal_disasteralerts.data.repository

import com.example.dizaal_disasteralerts.data.model.FeatureCollection
import com.example.dizaal_disasteralerts.data.network.RetrofitClient
import retrofit2.Call

class EarthquakeRepository {
    private val api = RetrofitClient.earthquakeApiService

    fun getEarthquakes(params: Map<String, String>): Call<FeatureCollection> {
        return api.getEarthquakes(params)
    }
}
