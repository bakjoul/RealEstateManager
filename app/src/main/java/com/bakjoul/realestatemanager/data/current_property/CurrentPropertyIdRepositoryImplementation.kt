package com.bakjoul.realestatemanager.data.current_property

import com.bakjoul.realestatemanager.domain.current_property.CurrentPropertyIdRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPropertyIdRepositoryImplementation @Inject constructor() : CurrentPropertyIdRepository {

    private val currentPropertyIdMutableStateFlow = MutableStateFlow<Long?>(null)
    private val currentPropertyIdChannel = Channel<Long>()

    override fun getCurrentPropertyId(): StateFlow<Long?> = currentPropertyIdMutableStateFlow.asStateFlow()

    override fun setCurrentPropertyId(currentId: Long?) {
        currentPropertyIdMutableStateFlow.value = currentId
        if (currentId != null) {
            currentPropertyIdChannel.trySend(currentId)
        }
    }

    override fun getCurrentPropertyIdChannel(): Channel<Long> = currentPropertyIdChannel
}
