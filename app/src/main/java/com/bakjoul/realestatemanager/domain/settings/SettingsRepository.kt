package com.bakjoul.realestatemanager.domain.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    suspend fun setCurrency(currency: String)

    suspend fun getCurrencyFlow(): Flow<String>
}
