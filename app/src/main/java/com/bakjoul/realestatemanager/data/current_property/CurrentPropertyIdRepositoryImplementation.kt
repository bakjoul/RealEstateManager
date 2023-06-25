package com.bakjoul.realestatemanager.data.current_property

import com.bakjoul.realestatemanager.domain.current_property.CurrentPropertyIdRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPropertyIdRepositoryImplementation @Inject constructor() :
    CurrentPropertyIdRepository {

    private val currentPropertyIdMutableStateFlow: MutableStateFlow<Long> = MutableStateFlow(-1)
    private val currentPropertyIdChannel: Channel<Long> = Channel()

    override fun getCurrentPropertyIdFlow(): Flow<Long> = currentPropertyIdMutableStateFlow.asStateFlow()

    override fun getCurrentPropertyIdChannel(): Channel<Long> = currentPropertyIdChannel

    override fun setCurrentPropertyId(currentId: Long) {
        currentPropertyIdMutableStateFlow.value = currentId
        currentPropertyIdChannel.trySend(currentId)
    }
}
