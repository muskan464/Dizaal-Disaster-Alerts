package com.example.dizaal_disasteralerts.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DisasterAlert(
    val type: String,            // e.g., Flood, Cyclone
    val title: String,
    val place: String,
    val postedTime: String,
    val riskLevel: String,       // High / Moderate / No info
    val headline: String,
    val info: String,
    val recommendedActions: String,
    val safetyTips: String,
    val sourceInfo: String,
    val sourceRecommended: String,
    val sourceSafety: String
) : Parcelable