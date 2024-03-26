package com.bakjoul.realestatemanager.domain.photos

import android.util.Log
import com.bakjoul.realestatemanager.domain.photos.content_resolver.SavePhotosToAppFilesUseCase
import com.bakjoul.realestatemanager.domain.photos.edit.InitExistingPropertyDraftPhotosUseCase
import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CopyPhotosToPhotoDraftsUseCase @Inject constructor(
    private val getPhotosForPropertyIdUseCase: GetPhotosForPropertyIdUseCase,
    private val savePhotosToAppFilesUseCase: SavePhotosToAppFilesUseCase,
    private val initExistingPropertyDraftPhotosUseCase: InitExistingPropertyDraftPhotosUseCase
) {

    private companion object {
        const val TAG = "CopyPhotosToDraftsUC"
    }

    suspend fun invoke(propertyId: Long): List<PhotoEntity>? {
        val photoEntities = getPhotosForPropertyIdUseCase.invoke(propertyId).first()
        // Duplicate photos files
        val copiedPhotoUris = savePhotosToAppFilesUseCase.invoke(photoEntities.map { it.uri }, true)

        return if (copiedPhotoUris != null) {
            val photoDrafts = photoEntities.mapIndexed { index, photoEntity ->
                PhotoEntity(0, propertyId, copiedPhotoUris[index], photoEntity.description)
            }

            // Inserts photo drafts in temporary photos table
            val photoDraftIds = initExistingPropertyDraftPhotosUseCase.invoke(photoDrafts)

            if (photoDraftIds != null && photoDraftIds.size == photoDrafts.size) {
                val updatedPhotoDrafts = mutableListOf<PhotoEntity>()
                // Updates photo drafts with their new ids
                photoDrafts.forEachIndexed { index, photoDraft ->
                    updatedPhotoDrafts.add(photoDraft.copy(id = photoDraftIds[index]))
                }
                updatedPhotoDrafts
            } else {
                Log.e(TAG, "Error initializing existing property draft photos")
                null
            }
        } else {
            Log.e(TAG, "Error duplicating property photos files")
            null
        }
    }
}
