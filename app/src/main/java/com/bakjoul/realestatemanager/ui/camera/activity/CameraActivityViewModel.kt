package com.bakjoul.realestatemanager.ui.camera.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.camera.ShouldShowPhotoPreviewUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraActivityViewModel @Inject constructor(
    private val shouldShowPhotoPreviewUseCase: ShouldShowPhotoPreviewUseCase
) : ViewModel() {

    val viewActionLiveData: LiveData<Event<CameraActivityViewAction>> = liveData {
        shouldShowPhotoPreviewUseCase.invoke().collect {
            if (it) {
                emit(Event(CameraActivityViewAction.ShowPhotoPreview))
            }
        }
    }
}
