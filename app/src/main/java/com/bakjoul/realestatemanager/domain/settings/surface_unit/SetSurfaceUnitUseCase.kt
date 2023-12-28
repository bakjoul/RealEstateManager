package com.bakjoul.realestatemanager.domain.settings.surface_unit

import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import javax.inject.Inject

class SetSurfaceUnitUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    suspend fun invoke(surfaceUnit: SurfaceUnit) = settingsRepository.setSurfaceUnit(surfaceUnit)
}
