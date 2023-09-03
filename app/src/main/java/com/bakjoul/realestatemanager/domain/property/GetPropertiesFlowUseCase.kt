package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.data.property.PropertyDao
import com.bakjoul.realestatemanager.domain.property.model.PropertyWithPhotosEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPropertiesFlowUseCase @Inject constructor(private val propertyDao: PropertyDao) {
    fun invoke(): Flow<List<PropertyWithPhotosEntity>> = propertyDao.getProperties()
}
