package com.bakjoul.realestatemanager.ui.camera.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class CameraActivityViewModel @Inject constructor(getCurrentNavigationUseCase: GetCurrentNavigationUseCase) : ViewModel() {

    val viewActionLiveData: LiveData<Event<CameraActivityViewAction>> =
        getCurrentNavigationUseCase.invoke()
            .mapNotNull {
                when (it) {
                    is To.Preview -> Event(CameraActivityViewAction.ShowPhotoPreview)
                    is To.CloseCamera -> Event(CameraActivityViewAction.CloseCamera)
                    is To.ClosePhotoPreview -> Event(CameraActivityViewAction.ClosePhotoPreview)
                }
            }
            .asLiveData()
}
