package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhotosForPropertyIdUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    fun invoke(propertyId: Long): Flow<List<PhotoEntity>> = photoRepository.getPhotosForPropertyIdFlow(propertyId)
}
