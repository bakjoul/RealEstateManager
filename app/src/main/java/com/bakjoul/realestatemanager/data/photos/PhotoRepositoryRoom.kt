package com.bakjoul.realestatemanager.data.photos

import android.database.sqlite.SQLiteException
import com.bakjoul.realestatemanager.data.photos.model.PhotoDto
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
    private val photoDao: PhotoDao,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : PhotoRepository {

    override suspend fun addPhotos(photoEntities: List<PhotoEntity>): List<Long>? = withContext(coroutineDispatcherProvider.io) {
        try {
            val photoDtos = photoEntities.map { mapToPhotoDto(it) }
            photoDao.insert(photoDtos)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    override fun getPhotosForPropertyId(propertyId: Long): Flow<List<PhotoEntity>> =
        photoDao.getPhotosForPropertyId(propertyId).map {
            mapPhotoDtoToDomainEntities(it)
        }.flowOn(coroutineDispatcherProvider.io)

    override suspend fun deletePhotos(photoIds: List<Long>) = withContext(coroutineDispatcherProvider.io) {
        photoDao.delete(photoIds)
    }

    override suspend fun deleteAllPhotosForPropertyId(propertyId: Long) = withContext(coroutineDispatcherProvider.io) {
        photoDao.deleteAllPhotosForPropertyId(propertyId)
    }

    override suspend fun updatePhotoDescription(photoId: Long, description: String): Int = withContext(coroutineDispatcherProvider.io) {
        photoDao.updatePhotoDescription(photoId, description)
    }

    // region Mapping
    private fun mapToPhotoDto(photoEntity: PhotoEntity): PhotoDto =
        PhotoDto(
            propertyId = photoEntity.propertyId,
            uri = photoEntity.uri,
            description = photoEntity.description
        )

    private fun mapPhotoDtoToDomainEntities(photoDtoList: List<PhotoDto>) =
        photoDtoList.map {
            PhotoEntity(
                id = it.id,
                propertyId = it.propertyId,
                uri = it.uri,
                description = it.description
            )
        }
    // endregion Mapping
}
