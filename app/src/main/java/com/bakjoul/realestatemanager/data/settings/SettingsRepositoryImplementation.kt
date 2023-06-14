package com.bakjoul.realestatemanager.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class SettingsRepositoryImplementation @Inject constructor(
    private val dataStorePreferences: DataStore<Preferences>
) : SettingsRepository {

    private companion object {
        val KEY_CURRENCY = stringPreferencesKey("currency")
    }

    override suspend fun setCurrency(currency: String) {
        Result.runCatching {
            dataStorePreferences.edit { preferences ->
                preferences[KEY_CURRENCY] = currency
            }
        }
    }

    override suspend fun getCurrencyLiveData(): Result<String> {
        return Result.runCatching {
            val flow = dataStorePreferences.data
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
            val value = flow.firstOrNull() ?: ""
            value
        }
    }
}
