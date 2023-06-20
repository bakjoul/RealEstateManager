package com.bakjoul.realestatemanager.domain.currency_rate

import com.bakjoul.realestatemanager.domain.currency_rate.model.CurrencyRateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCachedEuroRateUseCase @Inject constructor(private val currencyRateRepository: CurrencyRateRepository) {
    fun invoke(): Flow<CurrencyRateEntity> = currencyRateRepository.getCachedEuroRateFlow().map { it }
}
