package com.bakjoul.realestatemanager.domain.currency_rate

import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetEuroRateUseCase @Inject constructor(private val currencyRateRepository: CurrencyRateRepository) {
    fun invoke(): Flow<CurrencyRateWrapper> = flow { emit(currencyRateRepository.getEuroRate()) }
}
