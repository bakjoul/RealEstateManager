package com.bakjoul.realestatemanager.data.geocoding.model

import com.google.gson.annotations.SerializedName

data class ViewportResponse(
    @SerializedName("northeast") val northeast: LocationResponse?,
    @SerializedName("southwest") val southwest: LocationResponse?
)
