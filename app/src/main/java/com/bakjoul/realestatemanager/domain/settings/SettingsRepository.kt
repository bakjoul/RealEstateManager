package com.bakjoul.realestatemanager.domain.settings

import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    suspend fun setCurrency(currency: AppCurrency)

    fun getCurrencyFlow(): Flow<AppCurrency?>

    suspend fun setSurfaceUnit(surfaceUnit: SurfaceUnit)

    fun getSurfaceUnitFlow(): Flow<SurfaceUnit?>
}
