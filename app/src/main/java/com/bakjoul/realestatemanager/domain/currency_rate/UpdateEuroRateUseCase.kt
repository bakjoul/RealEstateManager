package com.bakjoul.realestatemanager.domain.currency_rate

import android.util.Log
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateWrapper
import javax.inject.Inject

class UpdateEuroRateUseCase @Inject constructor(private val currencyRateRepository: CurrencyRateRepository) {

    private companion object {
        private const val TAG = "UpdateEuroRateUseCase"
    }

    suspend fun invoke() {
        when (val result = currencyRateRepository.getEuroRate()) {
            is CurrencyRateWrapper.Success -> {
                Log.i(TAG, "Euro exchange rate at $${result.currencyRateEntity.rate} on ${result.currencyRateEntity.updateDate}")
            }

            is CurrencyRateWrapper.Error -> {
                Log.d(TAG, "Error while updating Euro rate: ${result.exception.message}")
            }

            is CurrencyRateWrapper.Failure -> {
                Log.d(TAG, "Failed to update Euro rate")
            }
        }
    }
}
