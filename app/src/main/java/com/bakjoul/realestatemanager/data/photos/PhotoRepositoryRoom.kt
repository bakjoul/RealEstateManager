package com.bakjoul.realestatemanager.data.photos

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
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : PhotoRepository {

    override suspend fun addPhoto(photoEntity: PhotoEntity) = withContext(coroutineDispatcherProvider.io) {
        photoDao.insert(mapToPhotoDtoEntity(photoEntity))
    }

    override fun getPhotosForPropertyIdFlow(propertyId: Long): Flow<List<PhotoEntity>> {
        return photoDao.getPhotos(propertyId).map {
            mapToDomainEntities(it)
        }
    }

    // region Mapping
    private fun mapToPhotoDtoEntity(photoEntity: PhotoEntity): PhotoDtoEntity =
        PhotoDtoEntity(
            propertyId = photoEntity.propertyId,
            url = photoEntity.url,
            description = photoEntity.description
        )

    private fun mapToDomainEntities(photoDtoEntityList: List<PhotoDtoEntity>) =
        photoDtoEntityList.mapIndexed { index, photoDtoEntity ->
            PhotoEntity(
                id = index.toLong(),
                propertyId = photoDtoEntity.propertyId,
                url = photoDtoEntity.url,
                description = photoDtoEntity.description
            )
        }
    // endregion Mapping
}
