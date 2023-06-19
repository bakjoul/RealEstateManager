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
                Log.i(TAG, "Euro rate at ${result.currencyRateResponse.rates.eurResponse.rate} on ${result.currencyRateResponse.updatedDate}")
            }

            is CurrencyRateResponseWrapper.Error -> {
                Log.d("test", "currency rate error")
            }

            is CurrencyRateResponseWrapper.Failure -> {
                Log.d("test", "currency rate failure")
            }
        }

    }
}
