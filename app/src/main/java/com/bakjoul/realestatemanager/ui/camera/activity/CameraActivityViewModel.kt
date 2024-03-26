package com.bakjoul.realestatemanager.ui.camera.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class CameraActivityViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCurrentNavigationUseCase: GetCurrentNavigationUseCase
) : ViewModel() {

    private val isExistingProperty = savedStateHandle.get<Boolean>("isExistingProperty") ?: false

    val viewActionLiveData: LiveData<Event<CameraActivityViewAction>> =
        getCurrentNavigationUseCase.invoke()
            .mapNotNull {
                when (it) {
                    is To.PhotoPreview -> Event(CameraActivityViewAction.ShowPhotoPreview(it.propertyId, isExistingProperty))
                    is To.ClosePhotoPreview -> Event(CameraActivityViewAction.ClosePhotoPreview)
                    is To.CloseCamera -> Event(CameraActivityViewAction.CloseCamera)
                    else -> null
                }
            }
            .asLiveData()
}
