package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class UpdatePhotoDescriptionUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend fun invoke(photoId: Long, description: String): Int {
        return photoRepository.updatePhotoDescription(photoId, description)
    }
}
