package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.domain.photos.DeletePhotosUseCase
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import javax.inject.Inject

class DeletePropertyUseCase @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val deletePhotosUseCase: DeletePhotosUseCase
) {
    suspend fun invoke(property: PropertyEntity): Boolean {
        val isPropertyDeleted = propertyRepository.deleteProperty(property.id)
        return if (isPropertyDeleted > 0) {
            if (property.photos.isNotEmpty()) {
                deletePhotosUseCase.invoke(property.photos.map { it.id }, property.photos.map { it.uri })
            }
            true
        } else {
            false
        }
    }
}
