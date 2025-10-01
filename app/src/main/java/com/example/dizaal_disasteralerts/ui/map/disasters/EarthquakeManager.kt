package com.example.dizaal_disasteralerts.ui.map.disasters

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.example.dizaal_disasteralerts.data.model.Earthquake
import kotlin.math.max

class EarthquakeManager(private val map: GoogleMap) {

    private val markers = mutableListOf<Marker>()
    private val circles = mutableListOf<Circle>()

    fun showEarthquakes(list: List<Earthquake>) {
        clearEarthquakes()
        list.forEach { showEarthquake(it) }
    }

    fun showEarthquake(eq: Earthquake) {
        val lat = eq.latitude ?: return
        val lon = eq.longitude ?: return
        val mag = eq.magnitude ?: 0.0

        val title = "M${"%.1f".format(mag)} • ${eq.place ?: "Unknown"}"
        val snippet = buildString {
            eq.depthKm?.let { append("Depth: ${"%.1f".format(it)} km\n") }
            eq.time?.let { append("Time: ${java.util.Date(it)}") }
        }
        val markerOptions = MarkerOptions()
            .position(LatLng(lat, lon))
            .title(title)
            .snippet(snippet)
            .zIndex(10f)
            .icon(BitmapDescriptorFactory.defaultMarker(getMarkerHueForMagnitude(mag)))
        map.addMarker(markerOptions)?.let { markers.add(it) }

        val baseRadius = 2000.0
        val radiusMeters = max(500.0, baseRadius * Math.pow(10.0, (mag / 1.5)))
        val circleOptions = CircleOptions()
            .center(LatLng(lat, lon))
            .radius(radiusMeters)
            .strokeWidth(2f)
            .strokeColor(getStrokeColorForMagnitude(mag))
            .fillColor(getFillColorForMagnitude(mag))
            .zIndex(1f)
        map.addCircle(circleOptions)?.let { circles.add(it) }
    }

    private fun getMarkerHueForMagnitude(mag: Double): Float {
        return when {
            mag >= 6.0 -> BitmapDescriptorFactory.HUE_RED
            mag >= 5.0 -> BitmapDescriptorFactory.HUE_ORANGE
            mag >= 4.0 -> BitmapDescriptorFactory.HUE_YELLOW
            else -> BitmapDescriptorFactory.HUE_AZURE
        }
    }

    private fun getFillColorForMagnitude(mag: Double): Int {
        return when {
            mag >= 6.0 -> Color.argb(80, 200, 0, 0)
            mag >= 5.0 -> Color.argb(70, 255, 140, 0)
            mag >= 4.0 -> Color.argb(60, 255, 200, 0)
            else -> Color.argb(40, 0, 120, 255)
        }
    }

    private fun getStrokeColorForMagnitude(mag: Double): Int {
        return when {
            mag >= 6.0 -> Color.argb(200, 180, 0, 0)
            mag >= 5.0 -> Color.argb(180, 200, 120, 0)
            mag >= 4.0 -> Color.argb(160, 200, 160, 0)
            else -> Color.argb(120, 0, 80, 200)
        }
    }

    fun clearEarthquakes() {
        markers.forEach { it.remove() }
        circles.forEach { it.remove() }
        markers.clear()
        circles.clear()
    }
}
