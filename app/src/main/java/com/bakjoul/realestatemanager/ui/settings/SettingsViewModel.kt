package com.bakjoul.realestatemanager.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.SetCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.SetSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val setSurfaceUnitUseCase: SetSurfaceUnitUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    val viewActionLiveData: LiveData<Event<SettingsViewAction>> =
        getCurrentNavigationUseCase.invoke()
            .mapNotNull {
                when (it) {
                    is To.CloseSettings -> Event(SettingsViewAction.CloseSettings)
                    else -> null
                }
            }.asLiveData()

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

    fun onCloseButtonClicked() {
        navigateUseCase.invoke(To.CloseSettings)
    }
}
