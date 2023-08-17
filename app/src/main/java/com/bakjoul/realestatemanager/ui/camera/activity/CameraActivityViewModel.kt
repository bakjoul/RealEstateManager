package com.bakjoul.realestatemanager.ui.camera.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.camera.GetCapturedPhotoUriUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraActivityViewModel @Inject constructor(
    private val getCapturedPhotoUriUseCase: GetCapturedPhotoUriUseCase
) : ViewModel() {

    val viewActionLiveData: LiveData<Event<CameraActivityViewAction>> = liveData {
        getCapturedPhotoUriUseCase.invoke().collect {
            if (it != null) {
                emit(Event(CameraActivityViewAction.ShowCapturedPhoto))
            }
        }
    }
}
