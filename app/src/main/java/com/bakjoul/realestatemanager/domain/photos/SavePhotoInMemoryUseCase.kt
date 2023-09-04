package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class SavePhotoInMemoryUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    fun invoke(photoUrl: String, description: String) {
        photoRepository.addPhoto(photoUrl, description)
    }
}
