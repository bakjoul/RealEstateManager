package com.bakjoul.realestatemanager.ui.camera

import androidx.lifecycle.ViewModel
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.camera.SetCapturedPhotoUriUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val setCapturedPhotoUriUseCase: SetCapturedPhotoUriUseCase,
    private val navigateUseCase: NavigateUseCase,
) : ViewModel() {

    fun onImageSaved(uri: String?) = uri?.let {
        setCapturedPhotoUriUseCase.invoke(it)
        navigateUseCase.invoke(To.PhotoPreview)
    }

    fun onCloseButtonClicked() {
        navigateUseCase.invoke(To.CloseCamera)
    }
}
