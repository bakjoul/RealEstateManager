package com.bakjoul.realestatemanager.domain.property.drafts

import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import javax.inject.Inject

class DoesDraftExistForPropertyIdUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    suspend fun invoke(propertyId: Long): Boolean {
        return propertyRepository.doesDraftExistForPropertyId(propertyId)
    }
}
