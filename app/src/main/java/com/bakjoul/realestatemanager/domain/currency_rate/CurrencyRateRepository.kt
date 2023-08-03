package com.bakjoul.realestatemanager.domain.currency_rate

import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateWrapper
import com.bakjoul.realestatemanager.domain.currency_rate.model.CurrencyRateEntity
import kotlinx.coroutines.flow.Flow

interface CurrencyRateRepository {

    suspend fun getEuroRate(): CurrencyRateWrapper

    fun getCachedEuroRateFlow(): Flow<CurrencyRateEntity>
}
