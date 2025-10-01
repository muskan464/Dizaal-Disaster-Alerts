package com.example.dizaal_disasteralerts.data.network

import com.example.dizaal_disasteralerts.data.model.FeatureCollection
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface EarthquakeApiService {
    // USGS FDSN endpoint (returns GeoJSON when format=geojson)
    @GET("fdsnws/event/1/query")
    fun getEarthquakes(@QueryMap options: Map<String, String>): Call<FeatureCollection>
}
