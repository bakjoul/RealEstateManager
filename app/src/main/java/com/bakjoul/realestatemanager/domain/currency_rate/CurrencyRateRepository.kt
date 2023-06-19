package com.bakjoul.realestatemanager.domain.currency_rate

import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateResponseWrapper
import kotlinx.coroutines.flow.Flow

interface CurrencyRateRepository {

    suspend fun setEuroRateLastUpdate(date: String)

    fun getEuroRateLastUpdateFlow(): Flow<String?>

    suspend fun getEuroRate(): CurrencyRateResponseWrapper

    fun getCachedEuroRateFlow(): Flow<String?>
}
