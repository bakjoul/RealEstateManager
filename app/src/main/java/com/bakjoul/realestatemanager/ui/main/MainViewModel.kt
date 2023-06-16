package com.bakjoul.realestatemanager.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bakjoul.realestatemanager.domain.current_property.GetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getCurrentPropertyIdUseCase: GetCurrentPropertyIdUseCase,
    isTabletUseCase: IsTabletUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
) : ViewModel() {


    val mainViewActionLiveData: LiveData<Event<MainViewAction>> =
        combine(
            getCurrentPropertyIdUseCase.invoke(),
            isTabletUseCase.invoke()
        ) { id, isTablet ->
            if (id >= 0) {
                if (isTablet) {
                    MainViewAction.DisplayDetailsFragment
                } else {
                    MainViewAction.NavigateToDetails
                }
            } else {
                null
            }
        }.map { action ->
            action?.let { Event(it) }
        }.filterNotNull().asLiveData()


    fun onResume(isTablet: Boolean) {
        refreshOrientationUseCase.invoke(isTablet)
    }
}
