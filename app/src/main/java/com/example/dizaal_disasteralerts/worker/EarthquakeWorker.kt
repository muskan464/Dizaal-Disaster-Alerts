// file: worker/EarthquakeWorker.kt
package com.example.dizaal_disasteralerts.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dizaal_disasteralerts.data.repository.EarthquakeRepository
import com.example.dizaal_disasteralerts.data.model.FeatureCollection
import com.example.dizaal_disasteralerts.data.model.toEarthquakes
import com.example.dizaal_disasteralerts.util.EarthquakeNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class EarthquakeWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    private val repo = EarthquakeRepository()
    private val prefs = appContext.getSharedPreferences("eq_prefs", Context.MODE_PRIVATE)
    private val processedKey = "processed_ids"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // build ISO times (UTC)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val end = Date()
            val start = Date(end.time - TimeUnit.MINUTES.toMillis(15)) // last 15 minutes
            val params = mapOf(
                "format" to "geojson",
                "starttime" to sdf.format(start),
                "endtime" to sdf.format(end),
                "minmagnitude" to (inputData.getDouble("minMagnitude", 4.0)).toString()
            )

            val call = repo.getEarthquakes(params)
            val resp = call.execute()
            if (!resp.isSuccessful) return@withContext Result.retry()
            val fc = resp.body()
            val events = fc.toEarthquakes()

            // dedupe with SharedPreferences set
            val processed = prefs.getStringSet(processedKey, emptySet())?.toMutableSet() ?: mutableSetOf()

            var anyNew = false
            for (eq in events) {
                if (!processed.contains(eq.id)) {
                    // new event -> notify
                    EarthquakeNotificationHelper.show(applicationContext, eq)
                    // optional: save to Firebase
                    try {
                        val db = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Earthquakes")
                        db.child(eq.id).setValue(eq)
                    } catch (_: Exception) { /* ignore if firebase not configured */ }

                    processed.add(eq.id)
                    anyNew = true
                }
            }

            if (anyNew) prefs.edit().putStringSet(processedKey, processed).apply()

            Result.success()
        } catch (ex: Exception) {
            ex.printStackTrace()
            Result.retry()
        }
    }
}
