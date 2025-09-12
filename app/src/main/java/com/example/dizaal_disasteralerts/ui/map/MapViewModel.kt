package com.example.dizaal_disasteralerts.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dizaal_disasteralerts.data.model.FloodResponse
import com.example.dizaal_disasteralerts.data.repository.FloodRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapViewModel : ViewModel() {

    private val repository = FloodRepository()

    private val _floodData = MutableLiveData<FloodResponse?>()
    val floodData: LiveData<FloodResponse?> = _floodData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchFloodData(lat: Double, lon: Double) {
        _loading.value = true
        repository.getFloodData(lat, lon).enqueue(object : Callback<FloodResponse> {
            override fun onResponse(call: Call<FloodResponse>, response: Response<FloodResponse>) {
                _loading.value = false
                if (response.isSuccessful) {
                    _floodData.value = response.body()
                } else {
                    _error.value = "Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<FloodResponse>, t: Throwable) {
                _loading.value = false
                _error.value = t.localizedMessage
            }
        })
    }
}
