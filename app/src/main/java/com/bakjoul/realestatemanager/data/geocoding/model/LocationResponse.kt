package com.bakjoul.realestatemanager.data.geocoding.model

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lng") val lng: Double?
)
