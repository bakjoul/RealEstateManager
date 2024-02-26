package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import javax.inject.Inject

class AddPhotosUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend fun invoke(propertyId: Long, photoUris: List<String>, description: String) : List<Long>? {
        val photoEntities = photoUris.map { PhotoEntity(0, propertyId, it, description) }
        return photoRepository.addPhotos(photoEntities)
    }
}
