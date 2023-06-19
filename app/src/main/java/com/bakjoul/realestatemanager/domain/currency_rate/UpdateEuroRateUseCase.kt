package com.bakjoul.realestatemanager.domain.currency_rate

import android.util.Log
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateResponseWrapper
import javax.inject.Inject

class UpdateEuroRateUseCase @Inject constructor(private val currencyRateRepository: CurrencyRateRepository) {

    suspend fun invoke() {
        when (val result = currencyRateRepository.getEuroRate()) {
            is CurrencyRateResponseWrapper.Success -> {
                Log.d("test", "currency rate success")
                Log.d("test", "euro rate: ${result.currencyRateResponse.rates.eurResponse.rate} ")
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
