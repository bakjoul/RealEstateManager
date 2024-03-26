package com.bakjoul.realestatemanager.domain.photos.edit

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import javax.inject.Inject

class AddPhotoToExistingPropertyDraftUseCase @Inject constructor(private val temporaryPhotoRepository: TemporaryPhotoRepository) {
    suspend fun invoke(propertyId: Long, photoUris: List<String>, description: String) : List<Long>? {
        val photoEntities = photoUris.map { PhotoEntity(0, propertyId, it, description) }
        return temporaryPhotoRepository.addPhotosToExistingPropertyDraft(photoEntities)
    }
}
