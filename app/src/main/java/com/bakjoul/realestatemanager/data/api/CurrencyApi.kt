package com.bakjoul.realestatemanager.data.api

import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface CurrencyApi {

    @GET
    suspend fun getCurrencyRate(
        @Url url: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("api_key") key: String
    ): CurrencyRateResponse
}
