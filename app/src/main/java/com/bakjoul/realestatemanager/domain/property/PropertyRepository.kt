package com.bakjoul.realestatemanager.domain.property

import kotlinx.coroutines.flow.StateFlow

interface PropertyRepository {

    fun getPropertiesStateFlow(): StateFlow<List<PropertyEntity>>

    fun getPropertyById(id: Long): PropertyEntity?
}
