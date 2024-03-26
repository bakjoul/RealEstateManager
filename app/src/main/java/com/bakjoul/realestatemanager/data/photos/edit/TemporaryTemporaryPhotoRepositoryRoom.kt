package com.bakjoul.realestatemanager.data.photos.edit

import android.database.sqlite.SQLiteException
import com.bakjoul.realestatemanager.data.photos.PhotoMapper
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.photos.edit.TemporaryPhotoRepository
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemporaryTemporaryPhotoRepositoryRoom @Inject constructor(
    private val photoMapper: PhotoMapper,
    private val temporaryPhotoDao: TemporaryPhotoDao,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : TemporaryPhotoRepository {

    override suspend fun initExistingPropertyDraftPhotos(photoDrafts: List<PhotoEntity>): List<Long>? = withContext(coroutineDispatcherProvider.io) {
        try {
            val photoDtos = photoDrafts.map { photoMapper.toTemporaryPhotoDto(it) }
            temporaryPhotoDao.insert(photoDtos)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun addPhotosToExistingPropertyDraft(photoEntities: List<PhotoEntity>): List<Long>? = withContext(coroutineDispatcherProvider.io) {
        try {
            val photoDtos = photoEntities.map { photoMapper.toTemporaryPhotoDto(it) }
            temporaryPhotoDao.insert(photoDtos)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    override fun getPhotosForExistingPropertyDraftId(propertyId: Long): Flow<List<PhotoEntity>> =
        temporaryPhotoDao.getPhotosForPropertyId(propertyId).map {
            photoMapper.tempDtoToDomainEntities(it)
        }.flowOn(coroutineDispatcherProvider.io)

    override suspend fun deletePhotosForExistingPropertyDraft(photoIds: List<Long>): Int? = withContext(coroutineDispatcherProvider.io) {
        try {
            temporaryPhotoDao.delete(photoIds)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun deleteAllPhotosForExistingPropertyDraftId(propertyId: Long): Int? = withContext(coroutineDispatcherProvider.io) {
        try {
            temporaryPhotoDao.deleteAllPhotosForPropertyId(propertyId)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun updatePhotoDescriptionForExistingPropertyDraft(photoId: Long, description: String): Int = withContext(coroutineDispatcherProvider.io) {
        temporaryPhotoDao.updatePhotoDescription(photoId, description)
    }
}
