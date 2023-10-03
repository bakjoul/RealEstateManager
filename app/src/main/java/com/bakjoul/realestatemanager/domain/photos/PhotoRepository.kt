package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    suspend fun addPhoto(photoEntity: PhotoEntity): Long?

    fun getPhotosForPropertyIdFlow(propertyId: Long): Flow<List<PhotoEntity>>

    suspend fun addPhotoDraft(photoEntity: PhotoEntity): Long?

    suspend fun hasPhotoDrafts(): Boolean

    fun getPhotosDrafts(): Flow<List<PhotoEntity>>

    suspend fun deletePhotoDraft(id: Long)

    suspend fun deletePhotoDraftsByIds(ids: List<Long>)

    suspend fun deleteAllPhotosDrafts()
}
