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

    override suspend fun addPhoto(photoEntity: PhotoEntity): Long? = withContext(coroutineDispatcherProvider.io) {
        try {
            photoDao.insert(mapToPhotoDto(photoEntity))
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    override fun getPhotosForPropertyId(propertyId: Long): Flow<List<PhotoEntity>> =
        photoDao.getPhotosForPropertyId(propertyId).map {
            mapPhotoDtoToDomainEntities(it)
        }.flowOn(coroutineDispatcherProvider.io)

    override suspend fun deletePhoto(photoId: Long) = withContext(coroutineDispatcherProvider.io) {
        photoDao.delete(photoId)
    }

    override suspend fun deleteAllPhotosForPropertyId(propertyId: Long) = withContext(coroutineDispatcherProvider.io) {
        photoDao.deleteAllPhotosForPropertyId(propertyId)
    }

    // region Mapping
    private fun mapToPhotoDto(photoEntity: PhotoEntity): PhotoDto =
        PhotoDto(
            propertyId = photoEntity.propertyId,
            url = photoEntity.url,
            description = photoEntity.description
        )

    private fun mapPhotoDtoToDomainEntities(photoDtoList: List<PhotoDto>) =
        photoDtoList.map {
            PhotoEntity(
                id = it.id,
                propertyId = it.propertyId,
                url = it.url,
                description = it.description
            )
        }
    // endregion Mapping
}
