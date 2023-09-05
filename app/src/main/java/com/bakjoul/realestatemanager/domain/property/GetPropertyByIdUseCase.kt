package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.data.property.PropertyRepositoryRoom
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import javax.inject.Inject

class GetPropertyByIdUseCase @Inject constructor(private val propertyRepositoryRoom: PropertyRepositoryRoom) {
    fun invoke(id: Long): PropertyEntity? = propertyRepositoryRoom.getPropertyById(id)
}
