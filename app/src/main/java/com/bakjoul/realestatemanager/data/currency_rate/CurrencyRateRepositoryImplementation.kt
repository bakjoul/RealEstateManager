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
import com.bakjoul.realestatemanager.data.currency_rate.model.CurrencyRateWrapper
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.currency_rate.CurrencyRateRepository
import com.bakjoul.realestatemanager.domain.currency_rate.model.CurrencyRateEntity
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
    private val application: Application
) : CurrencyRateRepository {

    companion object {
        private const val CURRENCY_RATE_DATA_STORE_NAME = "currency_rate_data_store"
        private const val DEFAULT_EURO_RATE = 1.0666

        private val DEFAULT_EURO_RATE_DATE = LocalDate.of(2023, 1, 13) // Source ECB Eurostat
        private val KEY_EUR_RATE = stringPreferencesKey("eur_rate")
        private val KEY_EUR_RATE_DATE = stringPreferencesKey("eur_rate_date")
    }

    private val Context.currencyRateDataStore: DataStore<Preferences> by preferencesDataStore(name = CURRENCY_RATE_DATA_STORE_NAME)

    override suspend fun getEuroRate(): CurrencyRateWrapper = withContext(Dispatchers.IO) {
        val cachedRate = getCachedEuroRateFlow().firstOrNull()

        if (cachedRate == null || cachedRate.updateDate != LocalDate.now()) {
            try {
                val response = currencyApi.getCurrencyRate(
                    "EUR",
                    "USD",
                    BuildConfig.CURRENCY_API_KEY
                )

                when {
                    response.status == "success" && response.rates?.usdResponse?.rate != null -> {
                        val rate = CurrencyRateEntity(
                            currency = AppCurrency.EUR,
                            rate = response.rates.usdResponse.rate.toDouble(),
                            updateDate = LocalDate.now(),
                        )

                        updateCachedEuroRate(rate)
                        CurrencyRateWrapper.Success(rate)
                    }
                    cachedRate != null -> CurrencyRateWrapper.Failure(
                        cachedRate,
                        "Failed to update Euro rate, last cached rate will be used (${cachedRate.rate} on ${cachedRate.updateDate})"
                    )
                    else -> CurrencyRateWrapper.Failure(getDefaultRate(), "Failed to update Euro rate, default rate will be used)")
                }
            } catch (e: Exception) {
                coroutineContext.ensureActive()

                CurrencyRateWrapper.Error(getDefaultRate(), e)
            }
        } else {
            CurrencyRateWrapper.Success(cachedRate)
        }
    }

    private fun getDefaultRate() = CurrencyRateEntity(
        currency = AppCurrency.EUR,
        rate = DEFAULT_EURO_RATE,
        updateDate = DEFAULT_EURO_RATE_DATE,
    )

    private suspend fun updateCachedEuroRate(currencyRate: CurrencyRateEntity) {
        val rate = currencyRate.rate.toString()
        val rateDate = currencyRate.updateDate.toString()
        try {
            application.currencyRateDataStore.edit { preferences ->
                preferences[KEY_EUR_RATE] = rate
                preferences[KEY_EUR_RATE_DATE] = rateDate
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getCachedEuroRateFlow(): Flow<CurrencyRateEntity?> = application.currencyRateDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val rate = preferences[KEY_EUR_RATE]
            val rateDate = preferences[KEY_EUR_RATE_DATE]
            if (rate != null && rateDate != null) {
                CurrencyRateEntity(
                    currency = AppCurrency.EUR,
                    rate = rate.toDouble(),
                    updateDate = LocalDate.parse(rateDate)
                )
            } else {
                null
            }
        }
}
