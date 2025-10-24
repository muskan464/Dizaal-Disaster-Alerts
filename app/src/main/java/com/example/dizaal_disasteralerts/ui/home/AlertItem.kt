package com.example.dizaal_disasteralerts.ui.home

import android.os.Parcel
import android.os.Parcelable

data class AlertItem(
    val title: String,
    val subtitle: String,
    val time: String,
    val severity: Severity,
    val payload: String? = null,

    val location: String? = null,
    val issuedTime: String? = null,
    val expectedTime: String? = null,
    val description: String? = null,
    val authority: String? = null,
    val instructions: String? = null,
    val shelterInfo: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        Severity.valueOf(parcel.readString() ?: Severity.FLOODS.name),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(subtitle)
        parcel.writeString(time)
        parcel.writeString(severity.name)
        parcel.writeString(payload)
        parcel.writeString(location)
        parcel.writeString(issuedTime)
        parcel.writeString(expectedTime)
        parcel.writeString(description)
        parcel.writeString(authority)
        parcel.writeString(instructions)
        parcel.writeString(shelterInfo)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AlertItem> {
        override fun createFromParcel(parcel: Parcel): AlertItem = AlertItem(parcel)
        override fun newArray(size: Int): Array<AlertItem?> = arrayOfNulls(size)
    }
}

enum class Severity {
    FLOODS,
    CYCLONES,
    EARTHQUAKES,
    HEATWAVES,
    COLDWAVES,
    TSUNAMIS
}
