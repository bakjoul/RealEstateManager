package com.bakjoul.realestatemanager.domain.photos

import javax.inject.Inject

class DeleteAllPhotosForPropertyIdUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    suspend fun invoke(propertyId: Long) = photoRepository.deleteAllPhotosForPropertyId(propertyId)
}
