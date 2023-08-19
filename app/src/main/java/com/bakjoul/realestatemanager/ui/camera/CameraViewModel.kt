package com.bakjoul.realestatemanager.ui.camera

import androidx.lifecycle.ViewModel
import com.bakjoul.realestatemanager.domain.camera.SetCapturedPhotoUriUseCase
import com.bakjoul.realestatemanager.domain.camera.SetShouldShowPhotoPreviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val setCapturedPhotoUriUseCase: SetCapturedPhotoUriUseCase,
    private val setShouldShowPhotoPreviewUseCase: SetShouldShowPhotoPreviewUseCase
) : ViewModel() {

    fun onImageSaved(uri: String?) = uri?.let {
        setCapturedPhotoUriUseCase.invoke(it)
        setShouldShowPhotoPreviewUseCase.invoke(true)
    }
}
