package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class ClearPhotosInMemoryUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    fun invoke() = photoRepository.clearPhotos()
}
