package com.bakjoul.realestatemanager.domain.settings

import com.bakjoul.realestatemanager.domain.settings.model.AppSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentSettingsUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    fun invoke(): Flow<AppSettings> = settingsRepository.getCurrentSettings()
}
