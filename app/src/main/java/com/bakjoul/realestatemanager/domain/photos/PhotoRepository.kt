package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    suspend fun addPhoto(photoEntity: PhotoEntity): Long?

    fun getPhotosForPropertyId(propertyId: Long): Flow<List<PhotoEntity>>

    suspend fun deletePhoto(photoId: Long)

    suspend fun deleteAllPhotosForPropertyId(propertyId: Long)
}
