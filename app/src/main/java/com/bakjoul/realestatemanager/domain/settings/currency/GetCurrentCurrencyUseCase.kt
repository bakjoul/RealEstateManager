package com.bakjoul.realestatemanager.domain.settings.currency

import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCurrentCurrencyUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    fun invoke(): Flow<AppCurrency> = settingsRepository.getCurrencyFlow().map { it ?: AppCurrency.USD }
}
