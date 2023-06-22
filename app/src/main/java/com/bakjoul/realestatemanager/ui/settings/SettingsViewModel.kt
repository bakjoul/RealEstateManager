package com.bakjoul.realestatemanager.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.SetCurrencyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase
) : ViewModel() {

    fun getCurrencyLiveData(): LiveData<AppCurrency> = getCurrentCurrencyUseCase.invoke().asLiveData()

    fun onCurrencySelected(currency: String) {
        viewModelScope.launch {
            setCurrencyUseCase.invoke(currency)
        }
    }
}
