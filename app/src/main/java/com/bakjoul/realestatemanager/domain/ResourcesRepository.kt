package com.bakjoul.realestatemanager.domain

import kotlinx.coroutines.flow.Flow

interface ResourcesRepository {

    fun isTabletFlow(): Flow<Boolean>

    fun refreshOrientation()
}
