package com.bakjoul.realestatemanager.domain.current_property

import kotlinx.coroutines.flow.Flow

interface CurrentPropertyIdRepository {
    fun setCurrentPropertyId(currentId: Long)

    fun resetCurrentPropertyId()

    fun getCurrentPropertyIdFlow(): Flow<Long>
}
