package com.example.dizaal_disasteralerts.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dizaal_disasteralerts.data.model.FloodResponse
import com.example.dizaal_disasteralerts.data.repository.FloodRepository
import com.example.dizaal_disasteralerts.ui.home.AlertItem
import com.example.dizaal_disasteralerts.ui.home.Severity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FloodViewModel : ViewModel() {

    private val floodRepository = FloodRepository()

    // For MapFragment (single)
    private val _floodDataSingle = MutableLiveData<FloodResponse?>()
    val floodDataSingle: LiveData<FloodResponse?> = _floodDataSingle

    // For HomeFragment (list of alerts)
    private val _floodAlerts = MutableLiveData<List<AlertItem>?>()
    val floodAlerts: LiveData<List<AlertItem>?> = _floodAlerts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchFloodData(lat: Double, lon: Double) {
        _loading.value = true
        floodRepository.getFloodData(lat, lon).enqueue(object : Callback<FloodResponse> {
            override fun onResponse(call: Call<FloodResponse>, response: Response<FloodResponse>) {
                _loading.value = false
                if (response.isSuccessful && response.body() != null) {
                    val floodResponse = response.body()

                    // Calculate risk level
                    val discharge = floodResponse?.daily?.riverDischarge?.firstOrNull() ?: 0.0
                    val maxDischarge = floodResponse?.daily?.riverDischargeMax?.firstOrNull() ?: discharge
                    val risk = calculateRisk(discharge, maxDischarge)

                    // Store in model
                    floodResponse?.riskLevel = risk
                    floodResponse?.severity = "Flood"
                    floodResponse?.area = "${lat}, ${lon}"
                    floodResponse?.message = "River discharge = $discharge, max = $maxDischarge"

                    // Update raw response (for map)
                    _floodDataSingle.value = floodResponse

                    // 🔹 Convert to AlertItem (for home list)
                    floodResponse?.let {
                        val alert = AlertItem(
                            title = "Flood Alert",
                            subtitle = risk,
                            time = it.daily?.time?.firstOrNull() ?: "Unknown",
                            severity = Severity.FLOODS,
                            location = it.area,
                            issuedTime = it.daily?.time?.firstOrNull(),
                            expectedTime = it.daily?.time?.lastOrNull(),
                            description = it.message,
                            authority = "Flood Monitoring Agency",
                            instructions = "Follow evacuation instructions if risk is High/Severe.",
                            shelterInfo = "Nearest shelters may be announced by authorities.",
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                        _floodAlerts.value = listOf(alert)
                    }

                } else {
                    _error.value = "Flood API error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<FloodResponse>, t: Throwable) {
                _loading.value = false
                _error.value = t.localizedMessage
            }
        })
    }

    /** 🔹 Centralized Risk Calculation */
    private fun calculateRisk(discharge: Double, maxDischarge: Double): String {
        return when {
            maxDischarge > 50 -> "Severe Flood Risk"
            maxDischarge > 20 -> "High Flood Risk"
            maxDischarge > 10 -> "Moderate Flood Risk"
            maxDischarge > 5 -> "Low Flood Risk"
            else -> "Minimal Flood Risk"
        }
    }
}
