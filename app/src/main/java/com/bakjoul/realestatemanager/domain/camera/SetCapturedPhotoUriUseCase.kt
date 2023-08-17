package com.bakjoul.realestatemanager.domain.camera

import javax.inject.Inject

class SetCapturedPhotoUriUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    fun invoke(uri: String) = cameraRepository.setCapturedPhotoUri(uri)
}
