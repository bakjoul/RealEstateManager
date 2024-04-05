package com.bakjoul.realestatemanager.domain.photos

import android.util.Log
import com.bakjoul.realestatemanager.domain.photos.content_resolver.PhotoFileRepository
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import javax.inject.Inject

class UpdatePhotosForPropertyIdUseCase @Inject constructor(
    private val photoFileRepository: PhotoFileRepository,
    private val photoRepository: PhotoRepository
) {
    private companion object {
        private const val TAG = "UpdatePhotosForPropUC"
    }

    suspend fun invoke(propertyId: Long, photosList: List<PhotoEntity>): List<Long>? {
        if (photosList.isEmpty()) {
            val areOriginalPhotosDeleted = photoRepository.deleteAllPhotosForPropertyId(propertyId)
            return if (areOriginalPhotosDeleted != null && areOriginalPhotosDeleted >= 0) {
                emptyList()
            } else {
                Log.e(TAG, "Couldn't delete original photos for property id $propertyId")
                null
            }
        }

        val newPhotoUris = photoFileRepository.moveTemporaryPhotosToMainDirectory(photosList.map { it.uri })
        if (newPhotoUris != null) {
            val updatedPhotos = photosList.mapIndexed { index, photoEntity ->
                photoEntity.copy(uri = newPhotoUris[index])
            }
            val areOriginalPhotosDeleted = photoRepository.deleteAllPhotosForPropertyId(propertyId)
            return if (areOriginalPhotosDeleted != null && areOriginalPhotosDeleted >= 0) {
                val addedPhotos = photoRepository.addPhotos(updatedPhotos)
                addedPhotos
            } else {
                Log.e(TAG, "Couldn't delete original photos for property id $propertyId")
                null
            }
        } else {
            Log.e(TAG, "Couldn't move temporary photos to main directory")
            return null
        }
    }
}
