package com.bakjoul.realestatemanager.domain.resources

import kotlinx.coroutines.flow.Flow

interface ResourcesRepository {

    fun isTabletFlow(): Flow<Boolean>

    fun setOrientation(isTablet: Boolean)
}
