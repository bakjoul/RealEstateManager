package com.bakjoul.realestatemanager.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.DistanceUnit
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.SetCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.distance_unit.GetCurrentDistanceUnitUseCase
import com.bakjoul.realestatemanager.domain.settings.distance_unit.SetDistanceUnitUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.SetSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
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
    private val getCurrentDistanceUnitUseCase: GetCurrentDistanceUnitUseCase,
    private val setDistanceUnitUseCase: SetDistanceUnitUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    val viewStateLiveData: LiveData<SettingsViewState> = liveData {
        combine(
            getCurrentCurrencyUseCase.invoke(),
            getCurrentSurfaceUnitUseCase.invoke(),
            getCurrentDistanceUnitUseCase.invoke()
        ) { currency, surfaceUnit, distanceUnit ->
            SettingsViewState(
                currency = currency,
                surfaceUnit = surfaceUnit,
                distanceUnit = distanceUnit
            )
        }.collect {
            emit(it)
        }
    }

    val viewActionLiveData: LiveData<Event<SettingsViewAction>> =
        getCurrentNavigationUseCase.invoke()
            .mapNotNull {
                when (it) {
                    is To.CloseSettings -> Event(SettingsViewAction.CloseSettings)
                    else -> null
                }
            }.asLiveData()

    fun onCurrencySelected(currency: AppCurrency) {
        viewModelScope.launch {
            setCurrencyUseCase.invoke(currency)
        }
    }

    fun onSurfaceUnitSelected(surfaceUnit: SurfaceUnit) {
        viewModelScope.launch {
            setSurfaceUnitUseCase.invoke(surfaceUnit)
        }
    }

    fun onDistanceUnitSelected(distanceUnit: DistanceUnit) {
        viewModelScope.launch {
            setDistanceUnitUseCase.invoke(distanceUnit)
        }
    }

    fun onCloseButtonClicked() {
        navigateUseCase.invoke(To.CloseSettings)
    }
}
