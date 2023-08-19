package com.bakjoul.realestatemanager.domain.camera

import javax.inject.Inject

class SetShouldShowPhotoPreviewUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    fun invoke(shouldShowPhotoPreview: Boolean) = cameraRepository.setShouldShowPhotoPreview(shouldShowPhotoPreview)
}
