package com.bakjoul.realestatemanager.domain.property.drafts

import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPropertyDraftsFlowUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    fun invoke(): Flow<List<PropertyFormEntity>> = propertyRepository.getPropertyDraftsFlow()
}
