package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class DeletePhotosFromAppFilesUseCase @Inject constructor(private val photoFileRepository: PhotoFileRepository) {
    suspend fun invoke(photoUris: List<String>) {
        return photoFileRepository.deletePhotosFromAppFiles(photoUris)
    }
}
