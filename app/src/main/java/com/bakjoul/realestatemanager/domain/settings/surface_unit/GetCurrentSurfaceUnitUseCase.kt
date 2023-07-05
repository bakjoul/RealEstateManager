package com.bakjoul.realestatemanager.domain.settings.surface_unit

import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCurrentSurfaceUnitUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    fun invoke(): Flow<SurfaceUnit> = settingsRepository.getSurfaceUnitFlow().map { it ?: SurfaceUnit.Feet }
}
