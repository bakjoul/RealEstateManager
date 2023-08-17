package com.bakjoul.realestatemanager.ui.camera

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bakjoul.realestatemanager.domain.camera.SetCapturedPhotoUriUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val setCapturedPhotoUriUseCase: SetCapturedPhotoUriUseCase
) : ViewModel() {

    fun onImageSaved(uri: Uri?) = uri?.let { setCapturedPhotoUriUseCase.invoke(it) }
}
