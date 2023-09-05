package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.data.property.PropertyRepositoryRoom
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPropertiesFlowUseCase @Inject constructor(private val propertyRepositoryRoom: PropertyRepositoryRoom) {
    fun invoke(): Flow<List<PropertyEntity>> = propertyRepositoryRoom.getPropertiesFlow()
}
