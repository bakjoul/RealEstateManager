package com.bakjoul.realestatemanager.ui.settings.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SettingsActivityViewModel @Inject constructor(
    isTabletUseCase: IsTabletUseCase,
    getCurrentNavigationUseCase: GetCurrentNavigationUseCase
) : ViewModel() {

    val viewActionLiveData: LiveData<Event<SettingsActivityViewAction>> =
        combine(
            isTabletUseCase.invoke(),
            getCurrentNavigationUseCase.invoke()
        ) { isTablet, navigation ->
            if (!isTablet) {
                when (navigation) {
                    is To.CloseSettings -> SettingsActivityViewAction.CloseSettings
                    else -> null
                }
            } else {
                null
            }
        }.filterNotNull().map {
            Event(it)
        }.asLiveData()
}
