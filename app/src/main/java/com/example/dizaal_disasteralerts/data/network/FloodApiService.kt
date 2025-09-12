package com.example.dizaal_disasteralerts.data.network

import com.example.dizaal_disasteralerts.data.model.FloodResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FloodApiService {
    @GET("v1/flood")
    fun getFloodData(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("daily") daily: String = "river_discharge,river_discharge_max,river_discharge_p75",
        @Query("forecast_days") forecastDays: Int = 16,
        @Query("ensemble") ensemble: Boolean = true,
        @Query("cell_selection") cellSelection: String = "land",
        @Query("timeformat") timeformat: String = "iso8601"
    ): Call<FloodResponse>
}
