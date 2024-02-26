package com.bakjoul.realestatemanager.domain.photos

interface PhotoFileRepository {

    suspend fun savePhotosToAppFiles(photoUris: List<String>): List<String>?

    suspend fun deletePhotosFromAppFiles(photoUris: List<String>)
}