package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.data.property.PropertyRepositoryRoom
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import javax.inject.Inject

class AddPropertyUseCase @Inject constructor(private val propertyRepositoryRoom: PropertyRepositoryRoom) {
    suspend fun invoke(property: PropertyEntity): Long {
        return propertyRepositoryRoom.add(property)
    }
}
