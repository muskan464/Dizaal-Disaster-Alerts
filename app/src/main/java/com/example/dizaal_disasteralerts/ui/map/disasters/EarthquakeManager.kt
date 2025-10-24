package com.example.dizaal_disasteralerts.ui.map.disasters

import android.graphics.Color
import com.example.dizaal_disasteralerts.data.model.Earthquake
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions

class EarthquakeManager(private val map: GoogleMap) {

    private var heatmapOverlay: TileOverlay? = null
    private val earthquakeMarkers = mutableListOf<Marker>()

    fun showEarthquakes(list: List<Earthquake>) {
        clearEarthquakes()
        if (list.isEmpty()) return

        // Convert earthquakes to WeightedLatLng points
        val weightedPoints = list.mapNotNull { eq ->
            val lat = eq.latitude ?: return@mapNotNull null
            val lon = eq.longitude ?: return@mapNotNull null
            val mag = eq.magnitude ?: 1.0

            WeightedLatLng(LatLng(lat, lon), mag.toFloat().toDouble())
        }

        if (weightedPoints.isEmpty()) return

        try {
            // green (low) → yellow → orange → red (high)
            val colors = intArrayOf(
                Color.argb(120, 0, 255, 0),    // green = low intensity
                Color.argb(140, 255, 215, 0),  // yellow
                Color.argb(160, 255, 165, 0),  // orange
                Color.argb(180, 255, 0, 0)     // red = high intensity
            )
            val startPoints = floatArrayOf(0.0f, 0.3f, 0.6f, 1.0f)
            val gradient = Gradient(colors, startPoints)


            val provider = HeatmapTileProvider.Builder()
                .weightedData(weightedPoints)
                .gradient(gradient)
                .radius(50)
                .opacity(0.6)
                .build()

            heatmapOverlay = map.addTileOverlay(TileOverlayOptions().tileProvider(provider))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val seenLocations = mutableSetOf<Pair<Double, Double>>()
        list.forEach { eq ->
            val lat = eq.latitude ?: return@forEach
            val lon = eq.longitude ?: return@forEach
            val key = Pair("%.3f".format(lat).toDouble(), "%.3f".format(lon).toDouble())
            if (!seenLocations.contains(key)) {
                seenLocations.add(key)
                addMarker(eq)
            }
        }
    }

    private fun addMarker(eq: Earthquake) {
        val lat = eq.latitude ?: return
        val lon = eq.longitude ?: return
        val mag = eq.magnitude ?: 0.0
        val depth = eq.depthKm ?: 0.0

        val marker = map.addMarker(
            MarkerOptions()
                .position(LatLng(lat, lon))
                .title("Earthquake Alert")
                .snippet("Magnitude: %.1f\nDepth: %.1f km".format(mag, depth))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .zIndex(10f)
        )
        marker?.let { earthquakeMarkers.add(it) }
    }

    fun clearEarthquakes() {
        heatmapOverlay?.remove()
        heatmapOverlay = null

        earthquakeMarkers.forEach { it.remove() }
        earthquakeMarkers.clear()
    }
}
