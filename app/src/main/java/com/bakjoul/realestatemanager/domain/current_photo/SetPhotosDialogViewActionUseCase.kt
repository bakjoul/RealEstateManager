package com.bakjoul.realestatemanager.domain.current_photo

import com.bakjoul.realestatemanager.ui.photos.PhotosDialogViewAction
import javax.inject.Inject

class SetPhotosDialogViewActionUseCase @Inject constructor(private val currentPhotoIdRepository: CurrentPhotoIdRepository) {
    fun invoke(viewAction: PhotosDialogViewAction) = currentPhotoIdRepository.setPhotosDialogViewAction(viewAction)
}
