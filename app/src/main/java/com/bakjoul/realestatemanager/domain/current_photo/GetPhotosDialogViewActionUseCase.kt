package com.bakjoul.realestatemanager.domain.current_photo

import com.bakjoul.realestatemanager.ui.photos.PhotosDialogViewAction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhotosDialogViewActionUseCase @Inject constructor(private val currentPhotoIdRepository: CurrentPhotoIdRepository) {
    fun invoke(): Flow<PhotosDialogViewAction> = currentPhotoIdRepository.getPhotosDialogViewActionFlow()
}
