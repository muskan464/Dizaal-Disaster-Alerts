package com.example.dizaal_disasteralerts.ui.home

data class AlertItem(
    val title: String,
    val subtitle: String,
    val time: String,
    val severity: Severity
)

enum class Severity { FLOODS, CYCLONES ,EARTHQUAKES ,HEATWAVES ,COLDWAVES ,TSUNAMIS }