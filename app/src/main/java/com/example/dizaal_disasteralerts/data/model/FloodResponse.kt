package com.example.dizaal_disasteralerts.data.model

import com.google.gson.annotations.SerializedName

data class FloodResponse(
    val latitude: Double?,
    val longitude: Double?,
    val generationtime_ms: Double?,
    val utc_offset_seconds: Int?,
    val timezone: String?,
    val timezone_abbreviation: String?,
    @SerializedName("daily_units")
    val dailyUnits: DailyUnits?,
    val daily: Daily?,
    // Friendly fields for UI (mutable, optional)
    var riskLevel: String? = null,
    var severity: String? = null,
    var area: String? = null,
    var message: String? = null
)

data class DailyUnits(
    val time: String?,
    @SerializedName("river_discharge") val riverDischarge: String?,
    @SerializedName("river_discharge_max") val riverDischargeMax: String?,
    @SerializedName("river_discharge_p75") val riverDischargeP75: String?
)

data class Daily(
    val time: List<String> = emptyList(),
    @SerializedName("river_discharge") val riverDischarge: List<Double>? = null,
    @SerializedName("river_discharge_max") val riverDischargeMax: List<Double>? = null,
    @SerializedName("river_discharge_p75") val riverDischargeP75: List<Double>? = null
)
