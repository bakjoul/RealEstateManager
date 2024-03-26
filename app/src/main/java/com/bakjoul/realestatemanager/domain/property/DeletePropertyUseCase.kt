package com.bakjoul.realestatemanager.domain.property

import javax.inject.Inject

class DeletePropertyUseCase @Inject constructor(private val propertyRepository: PropertyRepository, ) {
    suspend fun invoke(id: Long): Int {
        return propertyRepository.deleteProperty(id)
    }
}
