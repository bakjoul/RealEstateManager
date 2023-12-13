package com.bakjoul.realestatemanager.domain.property.drafts

import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import javax.inject.Inject

class GetPropertyDraftByIdUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    suspend fun invoke(id: Long): PropertyFormEntity? {
        return propertyRepository.getPropertyDraftById(id)
    }
}
