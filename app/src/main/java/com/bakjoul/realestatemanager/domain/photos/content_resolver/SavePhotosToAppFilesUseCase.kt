package com.bakjoul.realestatemanager.domain.photos.content_resolver

import javax.inject.Inject

class SavePhotosToAppFilesUseCase @Inject constructor(private val photoFileRepository: PhotoFileRepository) {
    suspend fun invoke(photoUris: List<String>, areTemporaryPhotos: Boolean): List<String>? {
        return photoFileRepository.savePhotosToAppFiles(photoUris, areTemporaryPhotos)
    }
}
