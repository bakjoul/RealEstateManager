package com.bakjoul.realestatemanager.domain.current_property

import kotlinx.coroutines.flow.Flow

interface CurrentPropertyIdRepository {

    fun getCurrentPropertyIdFlow(): Flow<Long>

    fun setCurrentPropertyId(currentId: Long)
}
