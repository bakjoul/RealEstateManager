package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class DeleteAllPhotosForPropertyIdUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val photoFileRepository: PhotoFileRepository
) {
    suspend fun invoke(propertyId: Long, photoUris: List<String>) {
        photoRepository.deleteAllPhotosForPropertyId(propertyId)
        photoFileRepository.deletePhotosFromAppFiles(photoUris)
    }
}
