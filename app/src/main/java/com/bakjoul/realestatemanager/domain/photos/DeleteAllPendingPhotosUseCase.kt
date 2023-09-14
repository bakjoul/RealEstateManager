package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class DeleteAllPendingPhotosUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend fun invoke() = photoRepository.deleteAllPendingPhotos()
}
