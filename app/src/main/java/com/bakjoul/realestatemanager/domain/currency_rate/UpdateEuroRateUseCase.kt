package com.bakjoul.realestatemanager.domain.currency_rate

import android.util.Log
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateResponseWrapper
import javax.inject.Inject

class UpdateEuroRateUseCase @Inject constructor(private val currencyRateRepository: CurrencyRateRepository) {

    suspend fun invoke() {
        currencyRateRepository.getEuroRateFlow().collect { result ->
            when (result) {
                is CurrencyRateResponseWrapper.Success -> {
                    Log.d("test", "success")
                    Log.d("test", "${result.currencyRateResponse.rates.eurResponse.rate} ")
                }

                is CurrencyRateResponseWrapper.Error -> {
                    Log.d("test", "error")
                }

                is CurrencyRateResponseWrapper.Failure -> {
                    Log.d("test", "failure")
                }
            }
        }
    }
}
