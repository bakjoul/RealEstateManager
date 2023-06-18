package com.bakjoul.realestatemanager.data.currency_rate.model

import com.google.gson.annotations.SerializedName

data class CurrencyRateResponse(
    @SerializedName("updated_date") val updatedDate: String,
    @SerializedName("rates") val rates: RatesResponse,
    @SerializedName("status") val status: String
)
