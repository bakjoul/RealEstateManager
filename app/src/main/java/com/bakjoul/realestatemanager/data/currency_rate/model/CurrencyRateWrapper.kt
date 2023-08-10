package com.bakjoul.realestatemanager.data.currency_rate.model

import com.bakjoul.realestatemanager.domain.currency_rate.model.CurrencyRateEntity

sealed class CurrencyRateWrapper {

    abstract val currencyRateEntity: CurrencyRateEntity

    data class Success(
        override val currencyRateEntity: CurrencyRateEntity
    ) : CurrencyRateWrapper()

    data class Failure(
        override val currencyRateEntity: CurrencyRateEntity,
        val message: String,
    ) : CurrencyRateWrapper()

    data class Error(
        override val currencyRateEntity: CurrencyRateEntity,
        val exception: Exception,
    ) : CurrencyRateWrapper()
}
