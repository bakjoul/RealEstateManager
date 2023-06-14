package com.bakjoul.realestatemanager.ui.settings

import androidx.lifecycle.ViewModel
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.SetCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase
) : ViewModel() {

    suspend fun getCurrentCurrencyFlow(): Flow<String> = getCurrencyUseCase.invoke()

    suspend fun onCurrencySelected(currency: String) {
        setCurrencyUseCase.invoke(currency)
    }
}
