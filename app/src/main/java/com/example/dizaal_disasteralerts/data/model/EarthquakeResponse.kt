package com.example.dizaal_disasteralerts.data.model

import com.google.gson.annotations.SerializedName

data class FeatureCollection(
    val type: String?,
    val metadata: Metadata?,
    val features: List<Feature>?
)

data class Metadata(
    val generated: Long?,
    val url: String?,
    val title: String?,
    val status: Int?,
    val api: String?,
    val count: Int?
)

data class Feature(
    val type: String?,
    val properties: Properties?,
    val geometry: Geometry?,
    val id: String?
)

data class Properties(
    @SerializedName("mag") val mag: Double?,
    val place: String?,
    val time: Long?,
    val updated: Long?,
    val tz: Int?,
    val url: String?,
    val detail: String?,
    val felt: Int?,
    val cdi: Double?,
    val mmi: Double?,
    val alert: String?,
    val status: String?,
    val tsunami: Int?,
    val sig: Int?,
    val net: String?,
    val code: String?,
    val title: String?
)

data class Geometry(
    val type: String?,
    val coordinates: List<Double>?
)

data class Earthquake(
    val id: String,
    val magnitude: Double?,
    val place: String?,
    val time: Long?,
    val latitude: Double?,
    val longitude: Double?,
    val depthKm: Double?
)

fun FeatureCollection?.toEarthquakes(): List<Earthquake> {
    if (this?.features == null) return emptyList()
    return this.features.mapNotNull { f ->
        val coords = f.geometry?.coordinates
        val lon = coords?.getOrNull(0)
        val lat = coords?.getOrNull(1)
        val depth = coords?.getOrNull(2)
        val id = f.id ?: return@mapNotNull null
        Earthquake(
            id = id,
            magnitude = f.properties?.mag,
            place = f.properties?.place,
            time = f.properties?.time,
            latitude = lat,
            longitude = lon,
            depthKm = depth
        )
    }
}
