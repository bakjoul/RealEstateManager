package com.bakjoul.realestatemanager.data.property

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPropertyIdRepository @Inject constructor() {

    private val currentPropertyMutableStateFlow = MutableStateFlow<Long?>(null)
    val currentPropertyIdFlow: StateFlow<Long?> = currentPropertyMutableStateFlow.asStateFlow()

    fun setCurrentPropertyId(currentId: Long) {
        currentPropertyMutableStateFlow.value = currentId
    }
}
