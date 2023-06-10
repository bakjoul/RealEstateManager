package com.bakjoul.realestatemanager.domain.current_property

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

interface CurrentPropertyIdRepository {

    fun getCurrentPropertyIdFlow(): Flow<Long>

    fun getCurrentPropertyIdChannel(): Channel<Long>

    fun setCurrentPropertyId(currentId: Long)
}
