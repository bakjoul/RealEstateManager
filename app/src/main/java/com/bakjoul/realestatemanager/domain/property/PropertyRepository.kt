package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {

    suspend fun add(propertyEntity: PropertyEntity): Long

    fun getPropertiesFlow(): Flow<List<PropertyEntity>>

    fun getPropertyById(id: Long): PropertyEntity?
}
