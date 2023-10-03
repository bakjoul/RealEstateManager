package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {

    suspend fun addProperty(propertyEntity: PropertyEntity): Long?

    fun getPropertiesFlow(): Flow<List<PropertyEntity>>

    fun getPropertyById(id: Long): Flow<PropertyEntity?>

    suspend fun addPropertyDraft(propertyForm: PropertyFormEntity): Long?

    suspend fun hasPropertyForms(): Boolean

    fun getPropertyFormsFlow(): Flow<List<PropertyFormEntity>>

    fun getPropertyFormById(id: Long): Flow<PropertyFormEntity?>

    suspend fun deletePropertyForm(id: Long)
}
