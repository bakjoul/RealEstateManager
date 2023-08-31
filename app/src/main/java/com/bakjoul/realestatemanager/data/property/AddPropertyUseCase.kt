package com.bakjoul.realestatemanager.data.property

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import javax.inject.Inject

class AddPropertyUseCase @Inject constructor(private val propertyDao: PropertyDao) {
    suspend fun invoke(property: PropertyEntity): Long {
        return propertyDao.insert(property)
    }
}
