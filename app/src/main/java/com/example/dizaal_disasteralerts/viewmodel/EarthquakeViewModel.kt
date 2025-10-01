package com.example.dizaal_disasteralerts.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dizaal_disasteralerts.data.model.Earthquake
import com.example.dizaal_disasteralerts.data.model.FeatureCollection
import com.example.dizaal_disasteralerts.data.model.toEarthquakes
import com.example.dizaal_disasteralerts.data.repository.EarthquakeRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class EarthquakeViewModel : ViewModel() {

    private val earthquakeRepository = EarthquakeRepository()

    private val _earthquakeData = MutableLiveData<List<Earthquake>?>()
    val earthquakeData: LiveData<List<Earthquake>?> = _earthquakeData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchEarthquakeData(
        lat: Double,
        lon: Double,
        minMagnitude: Double = 4.0,
        hoursBack: Long = 24,
        maxRadiusKm: Int = 500
    ) {
        _loading.value = true

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val end = Date()
        val start = Date(end.time - TimeUnit.HOURS.toMillis(hoursBack))

        val params = mapOf(
            "format" to "geojson",
            "starttime" to sdf.format(start),
            "endtime" to sdf.format(end),
            "minmagnitude" to minMagnitude.toString(),
            "latitude" to lat.toString(),
            "longitude" to lon.toString(),
            "maxradiuskm" to maxRadiusKm.toString(),
            "orderby" to "time"
        )

        earthquakeRepository.getEarthquakes(params).enqueue(object : Callback<FeatureCollection> {
            override fun onResponse(call: Call<FeatureCollection>, response: Response<FeatureCollection>) {
                _loading.value = false
                if (response.isSuccessful) {
                    _earthquakeData.value = response.body()?.toEarthquakes() ?: emptyList()
                } else {
                    _error.value = "Earthquake API error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<FeatureCollection>, t: Throwable) {
                _loading.value = false
                _error.value = t.localizedMessage
            }
        })
    }
}
