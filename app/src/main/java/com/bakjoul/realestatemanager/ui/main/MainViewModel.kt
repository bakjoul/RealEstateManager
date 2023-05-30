package com.bakjoul.realestatemanager.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyIdUseCase
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
        ) { currentPropertyId, isTablet ->
            if (currentPropertyId != null && !isTablet) {
                emit(Event(MainViewAction.NavigateToDetails))
            }
        }.collect()
    }

    fun getCurrentPropertyIdLiveData(): LiveData<Long?> = getCurrentPropertyIdUseCase.invoke().asLiveData()

    fun onResume() {
        refreshOrientationUseCase.invoke()
    }
}
