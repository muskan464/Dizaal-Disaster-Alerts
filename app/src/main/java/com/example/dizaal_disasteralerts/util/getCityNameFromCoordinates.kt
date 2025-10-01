package com.example.dizaal_disasteralerts.util
import android.content.Context
import android.location.Geocoder
import java.util.Locale

fun getCityNameFromCoordinates(context: Context, latitude: Double, longitude: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea ?: "Unknown"
        } else {
            "Unknown"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Unknown"
    }
}
