package com.bakjoul.realestatemanager.domain.camera

import javax.inject.Inject

class GetCapturedPhotoUriUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    fun invoke() = cameraRepository.getCapturedPhotoUri()
}
