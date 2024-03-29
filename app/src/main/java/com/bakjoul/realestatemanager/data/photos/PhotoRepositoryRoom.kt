package com.bakjoul.realestatemanager.data.photos

import android.database.sqlite.SQLiteException
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.photos.PhotoRepository
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryRoom @Inject constructor(
    private val photoMapper: PhotoMapper,
    private val photoDao: PhotoDao,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : PhotoRepository {

    override suspend fun addPhotos(photoEntities: List<PhotoEntity>): List<Long>? = withContext(coroutineDispatcherProvider.io) {
        try {
            val photoDtos = photoEntities.map { photoMapper.toPhotoDto(it) }
            photoDao.insert(photoDtos)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    override fun getPhotosForPropertyId(propertyId: Long): Flow<List<PhotoEntity>> =
        photoDao.getPhotosForPropertyId(propertyId).map {
            photoMapper.dtoToDomainEntities(it)
        }.flowOn(coroutineDispatcherProvider.io)

    override suspend fun deletePhotos(photoIds: List<Long>): Int? = withContext(coroutineDispatcherProvider.io) {
        try {
            photoDao.delete(photoIds)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun deleteAllPhotosForPropertyId(propertyId: Long): Int? = withContext(coroutineDispatcherProvider.io) {
        try {
            photoDao.deleteAllPhotosForPropertyId(propertyId)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun updatePhotoDescription(photoId: Long, description: String): Int = withContext(coroutineDispatcherProvider.io) {
        photoDao.updatePhotoDescription(photoId, description)
    }
}
