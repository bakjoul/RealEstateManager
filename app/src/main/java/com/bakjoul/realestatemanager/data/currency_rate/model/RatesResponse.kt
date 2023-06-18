package com.bakjoul.realestatemanager.data.currency_rate.model

import com.google.gson.annotations.SerializedName

data class RatesResponse(
    @SerializedName("EUR") val eurResponse: EurResponse
)
