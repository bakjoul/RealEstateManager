package com.bakjoul.realestatemanager.domain.photos.edit

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetPhotosForExistingPropertyDraftIdUseCase @Inject constructor(private val temporaryPhotoRepository: TemporaryPhotoRepository) {
    fun invoke(propertyId: Long?): Flow<List<PhotoEntity>> {
        return if (propertyId != null) {
            temporaryPhotoRepository.getPhotosForExistingPropertyDraftId(propertyId)
        } else {
            flowOf (emptyList())
        }
    }
}
