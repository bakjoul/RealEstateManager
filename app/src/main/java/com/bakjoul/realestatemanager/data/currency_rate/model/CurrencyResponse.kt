package com.bakjoul.realestatemanager.data.currency_rate.model

import com.google.gson.annotations.SerializedName

data class CurrencyResponse(
    @SerializedName("currency_name") val currencyName: String?,
    @SerializedName("rate") val rate: String?,
    @SerializedName("rate_for_amount") val rateForAmount: String?
)
