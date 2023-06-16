package com.bakjoul.realestatemanager.domain.settings.currency

import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import javax.inject.Inject

class SetCurrencyUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    suspend fun invoke(currency: String) =
        AppCurrency.values().find { it.nameWithSymbol == currency }?.let {
            settingsRepository.setCurrency(it)
        }
}
