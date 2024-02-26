package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class SavePhotosToAppFilesUseCase @Inject constructor(private val photoFileRepository: PhotoFileRepository) {
    suspend fun invoke(photoUris: List<String>): List<String>? {
        return photoFileRepository.savePhotosToAppFiles(photoUris)
    }
}
