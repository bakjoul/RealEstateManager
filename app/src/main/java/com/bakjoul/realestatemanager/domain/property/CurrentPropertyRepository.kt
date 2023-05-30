package com.bakjoul.realestatemanager.domain.property

import kotlinx.coroutines.flow.Flow

interface CurrentPropertyRepository {

    fun getCurrentPropertyId(): Flow<Long?>

    fun setCurrentPropertyId(currentId: Long?)
}
