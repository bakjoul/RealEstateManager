package com.bakjoul.realestatemanager.data.currency_rate.model

import com.bakjoul.realestatemanager.domain.currency_rate.model.CurrencyRateEntity

sealed class CurrencyRateWrapper {
    data class Success(val currencyRateEntity: CurrencyRateEntity) : CurrencyRateWrapper()
    data class Failure(val currencyRateEntity: CurrencyRateEntity, val message: String) : CurrencyRateWrapper()
    data class Error(val currencyRateEntity: CurrencyRateEntity, val exception: Exception) : CurrencyRateWrapper()
}
