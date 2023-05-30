package com.bakjoul.realestatemanager.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bakjoul.realestatemanager.data.ResourcesRepository
import com.bakjoul.realestatemanager.data.property.CurrentPropertyRepository
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    currentPropertyRepository: CurrentPropertyRepository,
    private val resourcesRepository: ResourcesRepository
) : ViewModel() {

    val mainViewActionLiveData: LiveData<Event<MainViewAction>?> = combine(
        currentPropertyRepository.getCurrentPropertyId(),
        resourcesRepository.isTabletFlow()
    ) { currentPropertyId, isTablet ->
        if (currentPropertyId != null && !isTablet) {
            Event<MainViewAction>(MainViewAction.NavigateToDetails)
        } else {
            null
        }
    }.asLiveData(Dispatchers.IO)

    fun onResume() {
        resourcesRepository.refreshOrientation()
    }
}
