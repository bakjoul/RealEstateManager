package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import javax.inject.Inject

class AddPropertyUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    suspend fun invoke(property: PropertyEntity): Long {
        return propertyRepository.addProperty(property)
    }
}
