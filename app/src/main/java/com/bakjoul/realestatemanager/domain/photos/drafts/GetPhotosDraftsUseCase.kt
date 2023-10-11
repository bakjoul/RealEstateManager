package com.bakjoul.realestatemanager.domain.photos.drafts

import com.bakjoul.realestatemanager.domain.photos.PhotoRepository
import javax.inject.Inject

class GetPhotosDraftsUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    fun invoke() = photoRepository.getPhotosDrafts()
}
