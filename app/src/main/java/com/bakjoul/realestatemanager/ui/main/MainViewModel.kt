package com.bakjoul.realestatemanager.ui.main

import android.util.Log
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
    private val getCurrentPropertyIdUseCase: GetCurrentPropertyIdUseCase,
    private val isTabletUseCase: IsTabletUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
) : ViewModel() {

    val mainViewActionLiveData: LiveData<Event<MainViewAction>> = liveData {
        combine(
            getCurrentPropertyIdUseCase.invoke(),
            isTabletUseCase.invoke()
        ) { propertyId, isTablet ->
            if (propertyId >= 0 && !isTablet) {
                Log.d("test", "we are in combine: propertyId: $propertyId")
                emit(Event(MainViewAction.NavigateToDetails))
            }
        }.collect()
    }

    fun getCurrentPropertyIdChannelLiveData(): LiveData<Long?> =
        getCurrentPropertyIdUseCase.invoke().asLiveData()

    fun onResume(isTablet: Boolean) {
        refreshOrientationUseCase.invoke(isTablet)
    }
}
