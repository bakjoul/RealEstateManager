package com.bakjoul.realestatemanager.domain.current_photo

import com.bakjoul.realestatemanager.ui.photos.PhotosDialogViewAction
import kotlinx.coroutines.flow.Flow

interface CurrentPhotoIdRepository {

    fun setCurrentPhotoId(currentId: Int)

    fun getCurrentPhotoIdFlow(): Flow<Int>

    fun setPhotosDialogViewAction(viewAction: PhotosDialogViewAction)

    fun getPhotosDialogViewActionFlow(): Flow<PhotosDialogViewAction>
}
