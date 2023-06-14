package com.bakjoul.realestatemanager.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImplementation @Inject constructor(
    private val dataStorePreferences: DataStore<Preferences>
) : SettingsRepository {

    private companion object {
        val DEFAULT_CURRENCY = AppCurrency.USD.nameWithSymbol
        val KEY_CURRENCY = stringPreferencesKey("currency")
    }

    private val currentCurrencyFlow = MutableSharedFlow<String>()

    override suspend fun setCurrency(currency: String) {
        Result.runCatching {
            dataStorePreferences.edit { preferences ->
                preferences[KEY_CURRENCY] = currency
            }
        }

        currentCurrencyFlow.emit(currency)
    }

    override suspend fun getCurrencyFlow(): Flow<String> = flow {
        val dataStoreFlow = dataStorePreferences.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[KEY_CURRENCY]
            }
        val initialValue = dataStoreFlow.first() ?: DEFAULT_CURRENCY
        emit(initialValue)

        currentCurrencyFlow.emit(initialValue)

        dataStoreFlow.collect { currency ->
            emit(currency ?: DEFAULT_CURRENCY)
            currentCurrencyFlow.emit(currency ?: DEFAULT_CURRENCY)
        }
    }
}
