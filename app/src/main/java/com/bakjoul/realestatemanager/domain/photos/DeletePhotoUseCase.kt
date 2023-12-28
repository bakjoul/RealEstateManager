package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class DeletePhotoUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend fun invoke(photoId: Long) {
        photoRepository.deletePhoto(photoId)
    }
}
