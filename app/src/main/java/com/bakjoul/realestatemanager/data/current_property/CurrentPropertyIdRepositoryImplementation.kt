package com.bakjoul.realestatemanager.data.current_property

import com.bakjoul.realestatemanager.domain.current_property.CurrentPropertyIdRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPropertyIdRepositoryImplementation @Inject constructor() : CurrentPropertyIdRepository {

    private val currentPropertyIdMutableStateFlow: MutableStateFlow<Long?> = MutableStateFlow(null)
    private val currentPropertyIdChannel: Channel<Long> = Channel()
    private val currentPropertyIdEventFlow: Flow<Long> = currentPropertyIdChannel.receiveAsFlow()

    override fun getCurrentPropertyIdFlowAsState(): Flow<Long> = currentPropertyIdMutableStateFlow.filterNotNull()

    override fun getCurrentPropertyIdFlowAsEvent(): Flow<Long> = currentPropertyIdEventFlow

    override fun setCurrentPropertyId(currentId: Long) {
        currentPropertyIdMutableStateFlow.value = currentId
        currentPropertyIdChannel.trySend(currentId)
    }
}
