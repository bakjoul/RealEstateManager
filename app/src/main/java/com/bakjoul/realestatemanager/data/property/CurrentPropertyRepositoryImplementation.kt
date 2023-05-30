package com.bakjoul.realestatemanager.data.property

import com.bakjoul.realestatemanager.domain.property.CurrentPropertyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPropertyRepositoryImplementation @Inject constructor() : CurrentPropertyRepository {

    private val currentPropertyMutableStateFlow = MutableStateFlow<Long?>(null)

    override fun getCurrentPropertyId(): Flow<Long?> = currentPropertyMutableStateFlow

    override fun setCurrentPropertyId(currentId: Long?) {
        currentPropertyMutableStateFlow.value = currentId
    }
}
