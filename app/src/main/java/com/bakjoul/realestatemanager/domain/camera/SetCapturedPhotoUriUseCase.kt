package com.bakjoul.realestatemanager.domain.camera

import android.net.Uri
import javax.inject.Inject

class SetCapturedPhotoUriUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    fun invoke(uri: Uri) = cameraRepository.setCapturedPhotoUri(uri)
}
