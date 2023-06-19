package com.bakjoul.realestatemanager.data.settings

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImplementation @Inject constructor(
    private val application: Application,
) : SettingsRepository {

    private companion object {
        private const val SETTINGS_DATA_STORE_NAME = "settings_data_store"
        private val KEY_CURRENCY = stringPreferencesKey("currency")
    }

    private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_DATA_STORE_NAME)

    override suspend fun setCurrency(currency: AppCurrency) {
        try {
            application.settingsDataStore.edit { preferences ->
                preferences[KEY_CURRENCY] = currency.name
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun getCurrencyFlow(): Flow<AppCurrency?> = application.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_CURRENCY]?.let {
                AppCurrency.valueOf(it)
            }
        }
}
