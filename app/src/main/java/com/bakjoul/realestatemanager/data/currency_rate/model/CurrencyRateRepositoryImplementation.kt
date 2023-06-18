package com.bakjoul.realestatemanager.data.currency_rate.model

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.data.api.CurrencyApi
import com.bakjoul.realestatemanager.domain.currency_rate.CurrencyRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRateRepositoryImplementation @Inject constructor(
    private val currencyApi: CurrencyApi,
    private val dataStorePreferences: DataStore<Preferences>
) :
    CurrencyRateRepository {

    companion object {
        const val BASE_URL = "https://api.getgeoapi.com/v2/currency/convert/"
        private val KEY_EUR_RATE_LAST_UPDATE = stringPreferencesKey("eur_rate_last_update")
        private val KEY_EUR_RATE = stringPreferencesKey("eur_rate")
    }

    override suspend fun setEuroRateLastUpdate(date: String) {
        try {
            dataStorePreferences.edit { preferences ->
                preferences[KEY_EUR_RATE_LAST_UPDATE] = date
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun getEuroRateLastUpdate(): Flow<String?> = dataStorePreferences.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_EUR_RATE_LAST_UPDATE]
        }

    override fun getEuroRateFlow(): Flow<CurrencyRateResponseWrapper> = flow {
        val lastUpdateDate = getEuroRateLastUpdate().firstOrNull()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastUpdateDate == null || lastUpdateDate != currentDate) {
            try {
                val response = currencyApi.getCurrencyRate(
                    BASE_URL,
                    "USD",
                    "EUR",
                    "json",
                    BuildConfig.CURRENCY_API_KEY
                )


                Log.d("test", "getEuroRateFlow: $response")

                if (response.status == "success") {
                    setEuroRateLastUpdate(currentDate)
                    setCachedEuroRate(response.rates.eurResponse.rate)
                    emit(CurrencyRateResponseWrapper.Success(response))
                } else {
                    emit(CurrencyRateResponseWrapper.Failure("Failed to get currency rate"))
                }

            } catch (e: Exception) {
                emit(CurrencyRateResponseWrapper.Error(e))
            }
        } else {
            Log.d("test", "getEuroRateFlow: cached !!!")
            val cachedRate = getCachedEuroRate().firstOrNull()
            if (cachedRate != null) {
                val cachedResponse = CurrencyRateResponse(
                    updatedDate = lastUpdateDate,
                    rates = RatesResponse(
                        eurResponse = EurResponse(
                            currencyName = "EUR",
                            rate = cachedRate,
                            rateForAmount = "1"
                        )
                    ),
                    status = "success"
                )
                emit(CurrencyRateResponseWrapper.Success(cachedResponse))
            } else {
                emit(CurrencyRateResponseWrapper.Failure("Failed to get currency rate"))
            }
        }

    }

    private suspend fun setCachedEuroRate(rate: String) {
        try {
            dataStorePreferences.edit { preferences ->
                preferences[KEY_EUR_RATE] = rate
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getCachedEuroRate(): Flow<String?> = dataStorePreferences.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_EUR_RATE]
        }
}

