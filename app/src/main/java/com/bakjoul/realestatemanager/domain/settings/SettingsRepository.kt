package com.bakjoul.realestatemanager.domain.settings

interface SettingsRepository {

    suspend fun setCurrency(currency: String)

    suspend fun getCurrencyLiveData(): Result<String>
}
