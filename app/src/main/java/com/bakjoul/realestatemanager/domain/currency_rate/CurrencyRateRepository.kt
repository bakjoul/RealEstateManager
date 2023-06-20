package com.bakjoul.realestatemanager.domain.currency_rate

import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateResponseWrapper
import com.bakjoul.realestatemanager.domain.currency_rate.model.CurrencyRateEntity
import kotlinx.coroutines.flow.Flow

interface CurrencyRateRepository {

    suspend fun getEuroRate(): CurrencyRateResponseWrapper

    fun getCachedEuroRateFlow(): Flow<CurrencyRateEntity>
}
