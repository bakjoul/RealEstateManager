package com.bakjoul.realestatemanager.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.current_property.GetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentPropertyIdChannelUseCase: GetCurrentPropertyIdUseCase,
    private val isTabletUseCase: IsTabletUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
) : ViewModel() {

    val mainViewActionLiveData: LiveData<Event<MainViewAction>> = liveData {
        combine(
            getCurrentPropertyIdChannelUseCase.invoke(),
            isTabletUseCase.invoke()
        ) { _, isTablet ->
            if (!isTablet) {
                emit(Event(MainViewAction.NavigateToDetails))
            }
        }.collect()
    }

    fun getCurrentPropertyIdChannelLiveData(): LiveData<Long?> =
        getCurrentPropertyIdChannelUseCase.invoke().asLiveData()

    fun onResume(isTablet: Boolean) {
        refreshOrientationUseCase.invoke(isTablet)
    }
}
