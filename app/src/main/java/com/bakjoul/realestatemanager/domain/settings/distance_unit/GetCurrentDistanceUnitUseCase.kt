package com.bakjoul.realestatemanager.domain.settings.distance_unit

import com.bakjoul.realestatemanager.data.settings.model.DistanceUnit
import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCurrentDistanceUnitUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    fun invoke(): Flow<DistanceUnit> = settingsRepository.getDistanceUnitFlow().map { it ?: DistanceUnit.KILOMETERS }
}
