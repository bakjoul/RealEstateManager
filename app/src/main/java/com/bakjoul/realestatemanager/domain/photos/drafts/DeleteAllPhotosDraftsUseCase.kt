package com.bakjoul.realestatemanager.domain.photos.drafts

import com.bakjoul.realestatemanager.domain.photos.PhotoRepository
import javax.inject.Inject

class DeleteAllPhotosDraftsUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend fun invoke() = photoRepository.deleteAllPhotosDrafts()
}
