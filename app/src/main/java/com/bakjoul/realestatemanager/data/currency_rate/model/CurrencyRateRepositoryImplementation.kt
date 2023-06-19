package com.bakjoul.realestatemanager.data.currency_rate.model

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.data.api.CurrencyApi
import com.bakjoul.realestatemanager.domain.currency_rate.CurrencyRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
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
    private val application: Application,
) : CurrencyRateRepository {



    companion object {
        private const val TAG = "CurrencyRateRepoImpl"

        private const val CURRENCY_RATE_DATA_STORE_NAME = "currency_rate_data_store"
        const val BASE_URL = "https://api.getgeoapi.com/v2/currency/convert/"
        private const val DEFAULT_EURO_RATE = "1.0666"  // Source ECB Eurostat 01/13/2023

        private val KEY_EUR_RATE_LAST_UPDATE = stringPreferencesKey("eur_rate_last_update")
        private val KEY_EUR_RATE = stringPreferencesKey("eur_rate")
    }

    private val Context.currencyRateDataStore: DataStore<Preferences> by preferencesDataStore(name = CURRENCY_RATE_DATA_STORE_NAME)

    override suspend fun setEuroRateLastUpdate(date: String) {
        try {
            application.currencyRateDataStore.edit { preferences ->
                preferences[KEY_EUR_RATE_LAST_UPDATE] = date
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun getEuroRateLastUpdate(): Flow<String?> = application.currencyRateDataStore.data
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

    override suspend fun getEuroRate(): CurrencyRateResponseWrapper {
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

                return if (response.status == "success") {
                    setEuroRateLastUpdate(currentDate)
                    setCachedEuroRate(response.rates.eurResponse.rate)
                    CurrencyRateResponseWrapper.Success(response)
                } else {
                    setCachedEuroRate(DEFAULT_EURO_RATE)
                    CurrencyRateResponseWrapper.Failure("Failed to get currency rate. Default rate will be used.")
                }

            } catch (e: Exception) {
                setCachedEuroRate(DEFAULT_EURO_RATE)
                Log.e(TAG, "An error occured. Default rate will be used.")
                return CurrencyRateResponseWrapper.Error(e)
            }
        } else {
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
                return CurrencyRateResponseWrapper.Success(cachedResponse)
            } else {
                return CurrencyRateResponseWrapper.Failure("Failed to get currency rate")
            }
        }

    }

    private suspend fun setCachedEuroRate(rate: String) {
        try {
            application.currencyRateDataStore.edit { preferences ->
                preferences[KEY_EUR_RATE] = rate
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getCachedEuroRate(): Flow<String?> = application.currencyRateDataStore.data
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

