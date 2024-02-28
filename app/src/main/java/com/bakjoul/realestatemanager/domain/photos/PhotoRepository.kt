package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    suspend fun addPhotos(photoEntities: List<PhotoEntity>): List<Long>?

    fun getPhotosForPropertyId(propertyId: Long): Flow<List<PhotoEntity>>

    suspend fun deletePhotos(photoIds: List<Long>)

    suspend fun deleteAllPhotosForPropertyId(propertyId: Long)

    suspend fun updatePhotoDescription(photoId: Long, description: String): Int
}
