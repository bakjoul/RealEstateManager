package com.bakjoul.realestatemanager.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.SetCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.SetSurfaceUnitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val setSurfaceUnitUseCase: SetSurfaceUnitUseCase
) : ViewModel() {

    fun getCurrencyLiveData(): LiveData<AppCurrency> = getCurrentCurrencyUseCase.invoke().asLiveData()
    fun getSurfaceUnitLiveData(): LiveData<SurfaceUnit> = getCurrentSurfaceUnitUseCase.invoke().asLiveData()

    fun onCurrencySelected(currency: String) {
        viewModelScope.launch {
            setCurrencyUseCase.invoke(currency)
        }
    }

    fun onSurfaceUnitSelected(unit: String) {
        viewModelScope.launch {
            setSurfaceUnitUseCase.invoke(unit)
        }
    }
}
