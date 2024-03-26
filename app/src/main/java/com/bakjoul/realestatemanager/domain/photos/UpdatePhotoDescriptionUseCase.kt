package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.photos.edit.TemporaryPhotoRepository
import javax.inject.Inject

class UpdatePhotoDescriptionUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val temporaryPhotoRepository: TemporaryPhotoRepository
) {
    suspend fun invoke(photoId: Long, description: String, isExistingProperty: Boolean): Int {
        return if (isExistingProperty) {
            temporaryPhotoRepository.updatePhotoDescriptionForExistingPropertyDraft(photoId, description)
        } else {
            photoRepository.updatePhotoDescription(photoId, description)
        }
    }
}
