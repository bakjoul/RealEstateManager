package com.bakjoul.realestatemanager.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.property.GetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentPropertyIdUseCase: GetCurrentPropertyIdUseCase,
    private val isTabletUseCase: IsTabletUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    coroutineDispatcherProvider: CoroutineDispatcherProvider
) : ViewModel() {

    // TODO What is the difference between the two implementations below?

    /*    val mainViewActionLiveData: LiveData<Event<MainViewAction>?> = combine(
            currentPropertyRepository.getCurrentPropertyId(),
            resourcesRepository.isTabletFlow()
        ) { currentPropertyId, isTablet ->
            if (currentPropertyId != null && !isTablet) {
                Event<MainViewAction>(MainViewAction.NavigateToDetails)
            } else {
                null
            }
        }.asLiveData(Dispatchers.IO)*/

    val mainViewActionLiveData: LiveData<Event<MainViewAction>?> = liveData(coroutineDispatcherProvider.io) {
            combine(
                getCurrentPropertyIdUseCase.invoke(),
                isTabletUseCase.invoke()
            ) { currentPropertyId, isTablet ->
                if (currentPropertyId != null && !isTablet) {
                    Event<MainViewAction>(MainViewAction.NavigateToDetails)
                } else {
                    null
                }
            }.collect {
                emit(it)
            }
        }

    fun onResume() {
        refreshOrientationUseCase.invoke()
    }
}
