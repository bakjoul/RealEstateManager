package com.bakjoul.realestatemanager.domain.current_property

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

interface CurrentPropertyIdRepository {

    fun getCurrentPropertyId(): Flow<Long?>

    fun setCurrentPropertyId(currentId: Long?)

    fun getCurrentPropertyIdChannel(): Channel<Long>
}
