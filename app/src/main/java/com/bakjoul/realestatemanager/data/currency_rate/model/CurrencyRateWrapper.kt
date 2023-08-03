package com.bakjoul.realestatemanager.data.currency_rate.model

import com.bakjoul.realestatemanager.domain.currency_rate.model.CurrencyRateEntity

sealed class CurrencyRateWrapper {
    data class Success(val currencyRateEntity: CurrencyRateEntity) : CurrencyRateWrapper()
    data class Failure(val message: String) : CurrencyRateWrapper()
    data class Error(val exception: Exception) : CurrencyRateWrapper()
}
