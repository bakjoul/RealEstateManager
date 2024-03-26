package com.bakjoul.realestatemanager.domain.photos.edit

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import javax.inject.Inject

class InitExistingPropertyDraftPhotosUseCase @Inject constructor(private val temporaryPhotoRepository: TemporaryPhotoRepository) {
    suspend fun invoke(photoDrafts: List<PhotoEntity>): List<Long>? {
        return temporaryPhotoRepository.initExistingPropertyDraftPhotos(photoDrafts)
    }
}
