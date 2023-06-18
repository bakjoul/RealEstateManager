package com.bakjoul.realestatemanager.data.currency_rate.model

sealed class CurrencyRateResponseWrapper {
    data class Success(val currencyRateResponse: CurrencyRateResponse) : CurrencyRateResponseWrapper()
    data class Failure(val message: String) : CurrencyRateResponseWrapper()
    data class Error(val throwable: Throwable) : CurrencyRateResponseWrapper()
}
