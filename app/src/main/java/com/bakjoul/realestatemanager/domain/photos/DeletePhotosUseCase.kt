package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class DeletePhotosUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val photoFileRepository: PhotoFileRepository
) {
    suspend fun invoke(photoIds: List<Long>, photoUris: List<String>) {
        photoRepository.deletePhotos(photoIds)
        photoFileRepository.deletePhotosFromAppFiles(photoUris)
    }
}
