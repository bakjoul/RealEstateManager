package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPropertyByIdUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    suspend fun invoke(id: Long): PropertyEntity? = propertyRepository.getPropertyById(id)
}
