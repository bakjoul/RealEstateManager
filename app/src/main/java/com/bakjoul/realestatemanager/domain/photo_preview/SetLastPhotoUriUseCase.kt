package com.bakjoul.realestatemanager.domain.photo_preview

import javax.inject.Inject

class SetLastPhotoUriUseCase @Inject constructor(private val photoPreviewRepository: PhotoPreviewRepository) {
    fun invoke(uri: String) = photoPreviewRepository.setLastPhotoUri(uri)
}
