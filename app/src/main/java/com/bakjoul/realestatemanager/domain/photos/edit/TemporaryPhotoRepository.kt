package com.bakjoul.realestatemanager.domain.photos.edit

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow

interface TemporaryPhotoRepository {

    suspend fun initExistingPropertyDraftPhotos(photoDrafts: List<PhotoEntity>): List<Long>?

    suspend fun addPhotosToExistingPropertyDraft(photoEntities: List<PhotoEntity>): List<Long>?

    fun getPhotosForExistingPropertyDraftId(propertyId: Long): Flow<List<PhotoEntity>>

    suspend fun deletePhotosForExistingPropertyDraft(photoIds: List<Long>): Int?

    suspend fun deleteAllPhotosForExistingPropertyDraftId(propertyId: Long): Int?

    suspend fun updatePhotoDescriptionForExistingPropertyDraft(photoId: Long, description: String): Int
}
