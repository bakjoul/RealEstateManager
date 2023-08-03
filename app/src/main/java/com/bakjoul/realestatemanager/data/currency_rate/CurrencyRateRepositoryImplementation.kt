package com.bakjoul.realestatemanager.data.currency_rate

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
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateWrapper
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.currency_rate.CurrencyRateRepository
import com.bakjoul.realestatemanager.domain.currency_rate.model.CurrencyRateEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
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

        private const val DEFAULT_EURO_RATE = 1.0666
        private val DEFAULT_EURO_RATE_DATE = LocalDate.of(2023, 1, 13) // Source ECB Eurostat

        private val KEY_EUR_RATE = stringPreferencesKey("eur_rate")
    }

    private val Context.currencyRateDataStore: DataStore<Preferences> by preferencesDataStore(name = CURRENCY_RATE_DATA_STORE_NAME)
    private val currencyRateType = object : TypeToken<CurrencyRateEntity>() {}.type

    override suspend fun getEuroRate(): CurrencyRateWrapper = withContext(Dispatchers.IO) {
        val cachedEuroRate = getCachedEuroRateFlow().firstOrNull()
        /*val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedCachedRateUpdate = cachedEuroRate?.updateDate?.format(formatter)*/

        if (cachedEuroRate == null || cachedEuroRate.updateDate != LocalDate.now()) {
            try {
                Log.d("test", "getEuroRate: $coroutineContext")
                val response = currencyApi.getCurrencyRate(
                    "EUR",
                    "USD",
                    BuildConfig.CURRENCY_API_KEY
                )

                return if (response.status == "success" && response.rates?.usdResponse?.rate != null) {
                    val rate = CurrencyRateEntity(
                        currency = AppCurrency.EUR,
                        rate = response.rates.usdResponse.rate.toDouble(),
                        updateDate = LocalDate.now(),
                    )
                    saveEuroRate(rate)
                    Log.i(TAG, "Euro exchange rate at $${rate.rate} on ${rate.updateDate}")
                    CurrencyRateWrapper.Success(rate)
                } else {
                    CurrencyRateWrapper.Failure("Failed to update Euro rate")
                }

            } catch (e: Exception) {
                coroutineContext.ensureActive()
                return CurrencyRateWrapper.Error(e)
            }
        } else {
            /*val cachedRate = CurrencyRateResponse(
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
            )*/
            val cachedRate = CurrencyRateEntity(
                currency = AppCurrency.EUR,
                rate = cachedEuroRate.rate,
                updateDate = cachedEuroRate.updateDate,
            )

            return CurrencyRateWrapper.Success(cachedRate)
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
