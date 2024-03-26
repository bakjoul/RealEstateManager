package com.bakjoul.realestatemanager.domain.photos.edit

import javax.inject.Inject

class DeleteAllPhotosForExistingPropertyDraftIdUseCase @Inject constructor(private val temporaryPhotoRepository: TemporaryPhotoRepository) {
    suspend fun invoke(propertyId: Long): Int? {
        return temporaryPhotoRepository.deleteAllPhotosForExistingPropertyDraftId(propertyId)
    }
}
