package com.bakjoul.realestatemanager.domain.settings.currency

import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrencyUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    suspend fun invoke(): Flow<String> = settingsRepository.getCurrencyFlow()
}
