package com.bakjoul.realestatemanager.domain.currency_rate.model

import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import java.time.LocalDate

data class CurrencyRateEntity(
    val currency: AppCurrency,
    val rate: Double,
    val updateDate: LocalDate
)
