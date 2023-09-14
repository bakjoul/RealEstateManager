package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class GetPendingPhotosUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    fun invoke() = photoRepository.getPendingPhotos()
}
