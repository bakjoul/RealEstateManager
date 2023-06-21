package com.bakjoul.realestatemanager.data.currency_rate

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.data.api.CurrencyApi
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateResponse
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateResponseWrapper
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyResponse
import com.bakjoul.realestatemanager.data.currency_rate.model.RatesResponse
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.currency_rate.CurrencyRateRepository
import com.bakjoul.realestatemanager.domain.currency_rate.model.CurrencyRateEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRateRepositoryImplementation @Inject constructor(
    private val currencyApi: CurrencyApi,
    private val application: Application,
    private val gson: Gson
) : CurrencyRateRepository {

    companion object {
        private const val CURRENCY_RATE_DATA_STORE_NAME = "currency_rate_data_store"
        const val BASE_URL = "https://api.getgeoapi.com/v2/currency/convert/"

        private const val DEFAULT_EURO_RATE = 1.0666
        private val DEFAULT_EURO_RATE_DATE = LocalDate.of(2023, 1, 13) // Source ECB Eurostat

        private val KEY_EUR_RATE = stringPreferencesKey("eur_rate")
    }

    private val Context.currencyRateDataStore: DataStore<Preferences> by preferencesDataStore(name = CURRENCY_RATE_DATA_STORE_NAME)
    private val currencyRateType = object : TypeToken<CurrencyRateEntity>() {}.type

    override suspend fun getEuroRate(): CurrencyRateResponseWrapper {
        val cachedEuroRate = getCachedEuroRateFlow().firstOrNull()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedCachedRateUpdate = cachedEuroRate?.updateDate?.format(formatter)

        if (cachedEuroRate == null || cachedEuroRate.updateDate != LocalDate.now()) {
            try {
                val response = currencyApi.getCurrencyRate(
                    BASE_URL,
                    "EUR",
                    "USD",
                    BuildConfig.CURRENCY_API_KEY
                )

                return if (response.status == "success" && response.rates?.usdResponse?.rate != null) {
                    val currencyRate = CurrencyRateEntity(
                        currency = AppCurrency.EUR,
                        rate = response.rates.usdResponse.rate.toDouble(),
                        updateDate = LocalDate.now(),
                    )
                    saveEuroRate(currencyRate)
                    CurrencyRateResponseWrapper.Success(response)
                } else {
                    CurrencyRateResponseWrapper.Failure("getEuroRate(): failure")
                }

            } catch (e: Exception) {
                return CurrencyRateResponseWrapper.Error(e)
            }
        } else {
            val cachedResponse = CurrencyRateResponse(
                updatedDate = formattedCachedRateUpdate ?: "",
                rates = RatesResponse(
                    usdResponse = CurrencyResponse(
                        currencyName = "USD",
                        rate = cachedEuroRate.rate.toString(),
                        rateForAmount = "1"
                    ),
                    eurResponse = null
                ),
                status = "success"
            )
            return CurrencyRateResponseWrapper.Success(cachedResponse)
        }
    }

    override fun getCachedEuroRateFlow(): Flow<CurrencyRateEntity> = application.currencyRateDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val currencyRateJson = preferences[KEY_EUR_RATE]
                if (currencyRateJson != null) {
                    gson.fromJson(currencyRateJson, currencyRateType)
                } else {
                    CurrencyRateEntity(
                        currency = AppCurrency.EUR,
                        rate = DEFAULT_EURO_RATE,
                        updateDate = DEFAULT_EURO_RATE_DATE,
                    )
                }
            }

    private suspend fun saveEuroRate(currencyRate: CurrencyRateEntity) {
        try {
            val currencyRateJson = gson.toJson(currencyRate)
            application.currencyRateDataStore.edit { preferences ->
                preferences[KEY_EUR_RATE] = currencyRateJson
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
