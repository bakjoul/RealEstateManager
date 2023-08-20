package com.bakjoul.realestatemanager.ui.camera.photo_preview

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.camera.DeleteCapturedPhotoUseCase
import com.bakjoul.realestatemanager.domain.camera.GetCapturedPhotoUriUseCase
import com.bakjoul.realestatemanager.domain.camera.SetCameraViewActionUseCase
import com.bakjoul.realestatemanager.ui.camera.activity.CameraActivityViewAction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PhotoPreviewViewModel @Inject constructor(
    private val getCapturedPhotoUriUseCase: GetCapturedPhotoUriUseCase,
    private val deleteCapturedPhotoUseCase: DeleteCapturedPhotoUseCase,
    private val setCameraViewActionUseCase: SetCameraViewActionUseCase
) : ViewModel() {

    val viewStateLiveData: LiveData<PhotoPreviewViewState> = liveData {
        getCapturedPhotoUriUseCase.invoke().collect {
            emit(PhotoPreviewViewState(it, null))
        }
    }

    fun onCancelButtonClicked(photoUri: String) {
        deleteCapturedPhotoUseCase.invoke(photoUri.toUri())
        setCameraViewActionUseCase.invoke(CameraActivityViewAction.ClosePhotoPreview)
    }

    fun onDoneButtonClicked() {
        setCameraViewActionUseCase.invoke(CameraActivityViewAction.ClosePhotoPreview)
    }
}
