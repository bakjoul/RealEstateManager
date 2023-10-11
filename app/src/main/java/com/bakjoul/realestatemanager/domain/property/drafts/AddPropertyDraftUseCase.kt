package com.bakjoul.realestatemanager.domain.property.drafts

import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import javax.inject.Inject

class AddPropertyDraftUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    suspend fun invoke(propertyForm: PropertyFormEntity): Long? {
        val insertedPropertyId = propertyRepository.addPropertyDraft(propertyForm)
        return if (insertedPropertyId != null && insertedPropertyId > 0) {
            insertedPropertyId
        } else {
            null
        }
    }
}
