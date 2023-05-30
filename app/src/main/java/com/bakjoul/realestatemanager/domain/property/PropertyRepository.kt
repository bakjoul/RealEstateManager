package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {

    fun getPropertiesStateFlow(): Flow<List<PropertyEntity>>

    suspend fun getPropertyById(id: Long): PropertyEntity?
}
