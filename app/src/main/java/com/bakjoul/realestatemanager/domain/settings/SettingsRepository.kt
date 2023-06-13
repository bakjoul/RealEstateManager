package com.bakjoul.realestatemanager.domain.settings

import androidx.lifecycle.LiveData

interface SettingsRepository {

    fun setCurrency(currency: String)

    fun getCurrencyLiveData(): LiveData<String>
}
