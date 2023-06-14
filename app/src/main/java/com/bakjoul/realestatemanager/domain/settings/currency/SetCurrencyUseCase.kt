package com.bakjoul.realestatemanager.domain.settings.currency

import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import javax.inject.Inject

class SetCurrencyUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    suspend fun invoke(currency: String) = settingsRepository.setCurrency(currency)
}
