package com.bakjoul.realestatemanager.data.current_property

import com.bakjoul.realestatemanager.domain.current_property.CurrentPropertyIdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPropertyIdRepositoryImplementation @Inject constructor() : CurrentPropertyIdRepository {

    private val currentPropertyIdMutableSharedFlow: MutableSharedFlow<Long?> = MutableSharedFlow(replay = 1)

    override fun setCurrentPropertyId(currentId: Long) {
        currentPropertyIdMutableSharedFlow.tryEmit(currentId)
    }

    override fun resetCurrentPropertyId() {
        currentPropertyIdMutableSharedFlow.tryEmit(null)
    }

    override fun getCurrentPropertyIdFlow(): Flow<Long> = currentPropertyIdMutableSharedFlow.asSharedFlow().filterNotNull()
}
