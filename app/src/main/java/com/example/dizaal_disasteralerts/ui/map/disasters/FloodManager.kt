package com.example.dizaal_disasteralerts.ui.map.disasters

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlin.math.cos
import kotlin.math.sin

class FloodManager(private val map: GoogleMap) {

    private val floodPolygons = mutableListOf<Polygon>()

    enum class RiskLevel { LOW, MODERATE, HIGH }

    fun showFlood(lat: Double, lon: Double, discharge: Double, maxDischarge: Double) {
        clearFloodPolygons()
        createFloodZone(lat, lon, discharge)
        if (maxDischarge > discharge * 1.5) {
            createFloodZone(lat + 0.005, lon + 0.005, maxDischarge)
        }
        createScatteredFloodZones(lat, lon, discharge)
        showFloodMarker(lat, lon, discharge)
    }

    private fun createFloodZone(centerLat: Double, centerLon: Double, discharge: Double) {
        val riskLevel = when {
            discharge > 3000 -> RiskLevel.HIGH
            discharge > 1500 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        val baseRadius = when (riskLevel) {
            RiskLevel.HIGH -> 0.015
            RiskLevel.MODERATE -> 0.01
            RiskLevel.LOW -> 0.008
        }
        val adjustedRadius = baseRadius * (1 + discharge / 5000.0)
        val polygonPoints = createIrregularPolygon(centerLat, centerLon, adjustedRadius)

        val polygonOptions = PolygonOptions()
            .addAll(polygonPoints)
            .fillColor(getFloodFillColor(riskLevel))
            .strokeColor(getFloodStrokeColor(riskLevel))
            .strokeWidth(3f)
            .zIndex(1f)

        map.addPolygon(polygonOptions)?.let { floodPolygons.add(it) }
    }

    private fun createScatteredFloodZones(centerLat: Double, centerLon: Double, discharge: Double) {
        val numZones = if (discharge > 2000) 5 else 3
        val smallRadius = 0.004

        for (i in 0 until numZones) {
            val angle = (360.0 / numZones) * i
            val distance = 0.02 + (i * 0.005)
            val zoneLat = centerLat + distance * cos(Math.toRadians(angle))
            val zoneLon = centerLon + distance * sin(Math.toRadians(angle))
            val polygonPoints = createIrregularPolygon(zoneLat, zoneLon, smallRadius)
            val riskLevel = if (discharge > 2500) RiskLevel.MODERATE else RiskLevel.LOW

            val polygonOptions = PolygonOptions()
                .addAll(polygonPoints)
                .fillColor(getFloodFillColor(riskLevel))
                .strokeColor(getFloodStrokeColor(riskLevel))
                .strokeWidth(2f)
                .zIndex(0.5f)

            map.addPolygon(polygonOptions)?.let { floodPolygons.add(it) }
        }
    }

    private fun createIrregularPolygon(centerLat: Double, centerLon: Double, baseRadius: Double): List<LatLng> {
        val points = mutableListOf<LatLng>()
        val numPoints = 12
        for (i in 0 until numPoints) {
            val angle = (360.0 / numPoints) * i
            val radius = baseRadius * (0.7 + Math.random() * 0.6)
            val lat = centerLat + radius * cos(Math.toRadians(angle))
            val lon = centerLon + radius * sin(Math.toRadians(angle))
            points.add(LatLng(lat, lon))
        }
        return points
    }

    private fun getFloodFillColor(riskLevel: RiskLevel) = when (riskLevel) {
        RiskLevel.HIGH -> Color.argb(100, 255, 0, 0)
        RiskLevel.MODERATE -> Color.argb(80, 255, 165, 0)
        RiskLevel.LOW -> Color.argb(60, 0, 100, 255)
    }

    private fun getFloodStrokeColor(riskLevel: RiskLevel) = when (riskLevel) {
        RiskLevel.HIGH -> Color.argb(180, 200, 0, 0)
        RiskLevel.MODERATE -> Color.argb(160, 200, 140, 0)
        RiskLevel.LOW -> Color.argb(140, 0, 80, 200)
    }

    private fun showFloodMarker(lat: Double, lon: Double, discharge: Double) {
        val riskLevel = when {
            discharge > 3000 -> "High Flood Risk"
            discharge > 1500 -> "Moderate Flood Risk"
            else -> "Low Flood Risk"
        }

        val markerOptions = MarkerOptions()
            .position(LatLng(lat, lon))
            .title("Flood Alert")
            .snippet("Risk: $riskLevel\nDischarge: $discharge m³/s")
            .icon(
                com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                    when {
                        riskLevel.contains("High") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                        riskLevel.contains("Moderate") -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE
                        else -> com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_BLUE
                    }
                )
            )
            .zIndex(10f)

        map.addMarker(markerOptions)?.showInfoWindow()
    }

    fun clearFloodPolygons() {
        floodPolygons.forEach { it.remove() }
        floodPolygons.clear()
    }
}
