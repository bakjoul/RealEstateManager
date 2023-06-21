package com.bakjoul.realestatemanager.domain.currency_rate

import android.util.Log
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateResponseWrapper
import javax.inject.Inject

class UpdateEuroRateUseCase @Inject constructor(private val currencyRateRepository: CurrencyRateRepository) {

    private companion object {
        private const val TAG = "UpdateEuroRateUseCase"
    }

    suspend fun invoke() {
        when (val result = currencyRateRepository.getEuroRate()) {
            is CurrencyRateResponseWrapper.Success -> {
                Log.i(TAG, "Euro rate at $${result.currencyRateResponse.rates?.usdResponse?.rate} on ${result.currencyRateResponse.updatedDate}")
            }

            is CurrencyRateResponseWrapper.Error -> {
                Log.d(TAG, "Error while updating Euro rate: ${result.throwable.message}")
            }

            is CurrencyRateResponseWrapper.Failure -> {
                Log.d(TAG, "Failed to update Euro rate")
            }
        }
    }
}
