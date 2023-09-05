package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import javax.inject.Inject

class AddPhotoUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend fun invoke(photo: PhotoEntity) = photoRepository.addPhoto(photo)
}
