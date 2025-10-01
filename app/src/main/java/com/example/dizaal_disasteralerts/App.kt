package com.example.dizaal_disasteralerts

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.dizaal_disasteralerts.worker.EarthquakeWorker
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Schedule earthquake checks every 15 minutes
        val request = PeriodicWorkRequestBuilder<EarthquakeWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "EarthquakeWorker",
            ExistingPeriodicWorkPolicy.KEEP, // don’t create duplicates
            request
        )
    }
}
