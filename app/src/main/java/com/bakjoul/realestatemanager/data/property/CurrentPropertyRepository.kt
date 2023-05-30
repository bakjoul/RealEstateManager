package com.bakjoul.realestatemanager.data.property

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPropertyRepository @Inject constructor() {

    private val currentPropertyMutableStateFlow = MutableStateFlow<Long?>(null)

    fun getCurrentPropertyId(): Flow<Long?> = currentPropertyMutableStateFlow

    fun setCurrentPropertyId(currentId: Long?) {
        currentPropertyMutableStateFlow.value = currentId
    }
}
