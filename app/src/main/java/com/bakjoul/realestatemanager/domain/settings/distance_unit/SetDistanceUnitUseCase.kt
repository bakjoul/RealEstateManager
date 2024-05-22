package com.bakjoul.realestatemanager.domain.settings.distance_unit

import com.bakjoul.realestatemanager.data.settings.model.DistanceUnit
import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import javax.inject.Inject

class SetDistanceUnitUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    suspend fun invoke(distanceUnit: DistanceUnit) = settingsRepository.setDistanceUnit(distanceUnit)
}
