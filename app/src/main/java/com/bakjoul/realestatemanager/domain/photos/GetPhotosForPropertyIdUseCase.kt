package com.bakjoul.realestatemanager.domain.photos

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetPhotosForPropertyIdUseCase @Inject constructor(private val photoRepository: PhotoRepository) {
    fun invoke(propertyId: Long?): Flow<List<PhotoEntity>> {
        return if (propertyId != null) {
            photoRepository.getPhotosForPropertyId(propertyId)
        } else {
            flowOf (emptyList())
        }
    }
}
