// file: util/EarthquakeNotificationHelper.kt
package com.example.dizaal_disasteralerts.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.dizaal_disasteralerts.R
import com.example.dizaal_disasteralerts.data.model.Earthquake

object EarthquakeNotificationHelper {
    private const val CHANNEL_ID = "earthquake_alerts"
    private const val CHANNEL_NAME = "Earthquake Alerts"

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                ch.description = "Receive alerts for new earthquakes"
                nm.createNotificationChannel(ch)
            }
        }
    }

    fun show(context: Context, eq: Earthquake) {
        ensureChannel(context)
        val magText = eq.magnitude?.let { "M${"%.1f".format(it)}" } ?: "M?"
        val title = "$magText • ${eq.place ?: "Unknown"}"
        val body = buildString {
            eq.depthKm?.let { append("Depth: ${"%.1f".format(it)} km • ") }
            eq.latitude?.let { append("lat:${"%.3f".format(it)} ") }
            eq.longitude?.let { append("lon:${"%.3f".format(eq.longitude)}") }
        }

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.dizaal_logo_with_no_bg) // add your app icon resource
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(eq.id.hashCode(), notif)
    }
}
