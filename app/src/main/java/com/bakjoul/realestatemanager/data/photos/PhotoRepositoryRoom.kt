package com.bakjoul.realestatemanager.data.photos

import com.bakjoul.realestatemanager.data.photos.model.PendingPhotoDtoEntity
import com.bakjoul.realestatemanager.data.photos.model.PhotoDtoEntity
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.photos.PhotoRepository
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryRoom @Inject constructor(
    private val photoDao: PhotoDao,
    private val pendingPhotoDao: PendingPhotoDao,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : PhotoRepository {

    override suspend fun addPhoto(photoEntity: PhotoEntity) = withContext(coroutineDispatcherProvider.io) {
        photoDao.insert(mapToPhotoDtoEntity(photoEntity))
    }

    override fun getPhotosForPropertyIdFlow(propertyId: Long): Flow<List<PhotoEntity>> {
        return photoDao.getPhotos(propertyId).map {
            mapPhotoDtoToDomainEntities(it)
        }
    }

    override suspend fun addPendingPhoto(photoEntity: PhotoEntity) {
        pendingPhotoDao.insert(mapToPendingPhotoDtoEntity(photoEntity))
    }

    override fun getPendingPhotos(): Flow<List<PhotoEntity>> {
        return pendingPhotoDao.getPendingPhotos().map {
            mapPendingPhotoDtoToDomainEntities(it)
        }
    }

    override suspend fun deletePendingPhoto(id: Long) {
        pendingPhotoDao.delete(id)
    }

    override suspend fun deleteAllPendingPhotos() {
        pendingPhotoDao.deleteAll()
    }

    // region Mapping
    private fun mapToPhotoDtoEntity(photoEntity: PhotoEntity): PhotoDtoEntity =
        PhotoDtoEntity(
            propertyId = photoEntity.propertyId,
            url = photoEntity.url,
            description = photoEntity.description
        )

    private fun mapPhotoDtoToDomainEntities(photoDtoEntityList: List<PhotoDtoEntity>) =
        photoDtoEntityList.mapIndexed { index, photoDtoEntity ->
            PhotoEntity(
                id = index.toLong(),
                propertyId = photoDtoEntity.propertyId,
                url = photoDtoEntity.url,
                description = photoDtoEntity.description
            )
        }

    private fun mapToPendingPhotoDtoEntity(photoEntity: PhotoEntity): PendingPhotoDtoEntity =
        PendingPhotoDtoEntity(
            propertyId = photoEntity.propertyId,
            url = photoEntity.url,
            description = photoEntity.description
        )

    private fun mapPendingPhotoDtoToDomainEntities(pendingPhotoDtoEntityList: List<PendingPhotoDtoEntity>) =
        pendingPhotoDtoEntityList.map {
            PhotoEntity(
                id = it.id,
                propertyId = it.propertyId,
                url = it.url,
                description = it.description
            )
        }
    // endregion Mapping
}
