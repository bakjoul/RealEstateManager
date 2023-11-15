package com.bakjoul.realestatemanager.domain.property.drafts

import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import javax.inject.Inject

class DeletePropertyDraftUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    suspend fun invoke(propertyId: Long) {
        propertyRepository.deletePropertyDraft(propertyId)
    }
}
