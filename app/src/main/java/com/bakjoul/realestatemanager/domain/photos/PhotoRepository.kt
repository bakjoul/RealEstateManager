package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    suspend fun addPhoto(photoEntity: PhotoEntity)

    fun getPhotosForPropertyIdFlow(propertyId: Long): Flow<List<PhotoEntity>>

    suspend fun addPendingPhoto(photoEntity: PhotoEntity)

    fun getPendingPhotos(): Flow<List<PhotoEntity>>

    suspend fun deletePendingPhoto(id: Long)

    suspend fun deleteAllPendingPhotos()
}
