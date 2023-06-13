package com.bakjoul.realestatemanager.data.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import com.bakjoul.realestatemanager.data.currency.Currency
import com.bakjoul.realestatemanager.data.utils.stringLiveData
import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImplementation @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    companion object {
        private const val KEY_CURRENCY = "currency"
    }

    override fun setCurrency(currency: String) {
        sharedPreferences.edit {
            putString(KEY_CURRENCY, currency)
            apply()
        }
    }

    override fun getCurrencyLiveData(): LiveData<String> {
        return sharedPreferences.stringLiveData(KEY_CURRENCY, Currency.Dollar.name)
    }
}
