package com.bakjoul.realestatemanager.ui.camera

import androidx.lifecycle.ViewModel
import com.bakjoul.realestatemanager.domain.camera.SetCameraViewActionUseCase
import com.bakjoul.realestatemanager.domain.camera.SetCapturedPhotoUriUseCase
import com.bakjoul.realestatemanager.ui.camera.activity.CameraActivityViewAction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val setCapturedPhotoUriUseCase: SetCapturedPhotoUriUseCase,
    private val setCameraViewActionUseCase: SetCameraViewActionUseCase
) : ViewModel() {

    fun onImageSaved(uri: String?) = uri?.let {
        setCapturedPhotoUriUseCase.invoke(it)
        setCameraViewActionUseCase.invoke(CameraActivityViewAction.ShowPhotoPreview)
    }

    fun onCloseButtonClicked() {
        setCameraViewActionUseCase.invoke(CameraActivityViewAction.CloseCamera)
    }
}
