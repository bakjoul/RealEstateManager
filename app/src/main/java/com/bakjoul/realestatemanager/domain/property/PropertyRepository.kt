package com.bakjoul.realestatemanager.domain.property

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.ui.main.MainViewAction
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {

    fun getPropertiesFlow(): Flow<List<PropertyEntity>>

    suspend fun getPropertyById(id: Long): PropertyEntity?

    fun setAddPropertyViewAction(action: MainViewAction)

    fun getAddPropertyViewActionFlow(): Flow<MainViewAction>
}
