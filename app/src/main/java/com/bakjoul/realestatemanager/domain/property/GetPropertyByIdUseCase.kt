package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import javax.inject.Inject

class GetPropertyByIdUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    suspend fun invoke(id: Long): PropertyEntity? = propertyRepository.getPropertyById(id)
}
