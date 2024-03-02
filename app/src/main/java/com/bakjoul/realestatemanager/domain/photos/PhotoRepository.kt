package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    suspend fun addPhotos(photoEntities: List<PhotoEntity>): List<Long>?

    fun getPhotosForPropertyId(propertyId: Long): Flow<List<PhotoEntity>>

    suspend fun deletePhotos(photoIds: List<Long>): Int?

    suspend fun deleteAllPhotosForPropertyId(propertyId: Long): Int?

    suspend fun updatePhotoDescription(photoId: Long, description: String): Int
}
