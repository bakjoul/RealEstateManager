package com.bakjoul.realestatemanager.domain.photos.content_resolver

interface PhotoFileRepository {

    suspend fun savePhotosToAppFiles(photoUris: List<String>, areTemporaryPhotos: Boolean): List<String>?

    suspend fun deletePhotosFromAppFiles(photoUris: List<String>)

    suspend fun moveTemporaryPhotosToMainDirectory(photoUris: List<String>): List<String>?
}
