package com.bakjoul.realestatemanager.domain.current_property

import kotlinx.coroutines.flow.Flow

interface CurrentPropertyIdRepository {

    fun getCurrentPropertyIdFlowAsState(): Flow<Long>

    fun getCurrentPropertyIdFlowAsEvent(): Flow<Long>

    fun setCurrentPropertyId(currentId: Long)
}
