package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class DeletePendingPhotoUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend fun invoke(id: Long) {
        photoRepository.deletePendingPhoto(id)
    }
}
