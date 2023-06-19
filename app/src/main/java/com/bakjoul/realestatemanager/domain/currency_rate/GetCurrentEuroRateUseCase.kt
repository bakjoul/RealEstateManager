package com.bakjoul.realestatemanager.domain.currency_rate

import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateRepositoryImplementation.Companion.DEFAULT_EURO_RATE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCurrentEuroRateUseCase @Inject constructor(private val currencyRateRepository: CurrencyRateRepository) {
    fun invoke(): Flow<String> = currencyRateRepository.getCachedEuroRateFlow().map { it ?: DEFAULT_EURO_RATE }
}
