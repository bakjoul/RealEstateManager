package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPropertiesFlowUseCase @Inject constructor(private val propertyRepository: PropertyRepository) {
    fun invoke(): Flow<List<PropertyEntity>> = propertyRepository.getPropertiesFlow()
}
