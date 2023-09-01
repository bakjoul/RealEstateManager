package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.data.property.PropertyDao
import com.bakjoul.realestatemanager.domain.property.model.PropertyWithPhotosEntity
import javax.inject.Inject

class GetPropertyByIdUseCase @Inject constructor(private val propertyDao: PropertyDao) {
    fun invoke(id: Long): PropertyWithPhotosEntity = propertyDao.getPropertyById(id)
}
