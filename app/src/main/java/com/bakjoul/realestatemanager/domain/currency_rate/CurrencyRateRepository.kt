package com.bakjoul.realestatemanager.domain.currency_rate

import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateWrapper

interface CurrencyRateRepository {

    suspend fun getEuroRate(): CurrencyRateWrapper
}
