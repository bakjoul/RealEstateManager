package com.bakjoul.realestatemanager.data.api

import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {

    @GET("convert")
    suspend fun getCurrencyRate(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("api_key") key: String
    ): CurrencyRateResponse
}
