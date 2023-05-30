package com.bakjoul.realestatemanager.domain.property

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPropertyByIdUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    fun invoke(id: Long) = propertyRepository.getPropertyById(id)
}
