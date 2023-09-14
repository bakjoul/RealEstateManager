package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import javax.inject.Inject

class AddPendingPhotoUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend fun invoke(photoUri: String, description: String) {
        photoRepository.addPendingPhoto(PhotoEntity(0, 0, photoUri, description))
    }
}
