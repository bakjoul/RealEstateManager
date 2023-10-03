package com.bakjoul.realestatemanager.data.photos

import android.database.sqlite.SQLiteException
import com.bakjoul.realestatemanager.data.photos.model.PhotoDraftDto
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
    private val photoDraftDao: PhotoDraftDao,
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

    override fun getPhotosForPropertyIdFlow(propertyId: Long): Flow<List<PhotoEntity>> =
        photoDao.getPhotos(propertyId).map {
            mapPhotoDtoToDomainEntities(it)
        }.flowOn(coroutineDispatcherProvider.io)

    override suspend fun addPhotoDraft(photoEntity: PhotoEntity): Long? = withContext(coroutineDispatcherProvider.io) {
        try {
            photoDraftDao.insert(mapToPhotoDraftDto(photoEntity))
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun hasPhotoDrafts(): Boolean = photoDraftDao.hasPhotoDrafts()

    override fun getPhotosDrafts(): Flow<List<PhotoEntity>> =
        photoDraftDao.getPhotoDrafts().map {
            mapPhotoDraftDtoToDomainEntities(it)
        }.flowOn(coroutineDispatcherProvider.io)

    override suspend fun deletePhotoDraft(id: Long) = withContext(coroutineDispatcherProvider.io) {
        photoDraftDao.delete(id)
    }

    override suspend fun deletePhotoDraftsByIds(ids: List<Long>) = withContext(coroutineDispatcherProvider.io) {
        photoDraftDao.deleteBulk(ids)
    }

    override suspend fun deleteAllPhotosDrafts() = withContext(coroutineDispatcherProvider.io) {
        photoDraftDao.deleteAll()
    }

    // region Mapping
    private fun mapToPhotoDto(photoEntity: PhotoEntity): PhotoDto =
        PhotoDto(
            propertyId = photoEntity.propertyId,
            url = photoEntity.url,
            description = photoEntity.description
        )

    private fun mapPhotoDtoToDomainEntities(photoDtoList: List<PhotoDto>) =
        photoDtoList.mapIndexed { index, photoDtoEntity ->
            PhotoEntity(
                id = index.toLong(),
                propertyId = photoDtoEntity.propertyId,
                url = photoDtoEntity.url,
                description = photoDtoEntity.description
            )
        }

    private fun mapToPhotoDraftDto(photoEntity: PhotoEntity): PhotoDraftDto =
        PhotoDraftDto(
            propertyFormId = photoEntity.propertyId,
            url = photoEntity.url,
            description = photoEntity.description
        )

    private fun mapPhotoDraftDtoToDomainEntities(photoDraftDtoList: List<PhotoDraftDto>) =
        photoDraftDtoList.map {
            PhotoEntity(
                id = it.id,
                propertyId = it.propertyFormId,
                url = it.url,
                description = it.description
            )
        }
    // endregion Mapping
}
