package com.bakjoul.realestatemanager.domain.settings

import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    suspend fun setCurrency(currency: AppCurrency)

    fun getCurrencyFlow(): Flow<AppCurrency?>
}
