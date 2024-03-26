package com.bakjoul.realestatemanager.domain.photos.edit

import javax.inject.Inject

class UpdatePhotoDescriptionForExistingPropertyDraft @Inject constructor(private val temporaryPhotoRepository: TemporaryPhotoRepository) {
    suspend fun invoke(photoId: Long, description: String): Int {
        return temporaryPhotoRepository.updatePhotoDescriptionForExistingPropertyDraft(photoId, description)
    }
}
