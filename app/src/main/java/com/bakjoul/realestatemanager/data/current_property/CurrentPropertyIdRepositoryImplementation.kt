package com.bakjoul.realestatemanager.data.current_property

import com.bakjoul.realestatemanager.domain.current_property.CurrentPropertyIdRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPropertyIdRepositoryImplementation @Inject constructor() : CurrentPropertyIdRepository {

    private val currentPropertyIdFlow = MutableSharedFlow<Long>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun getCurrentPropertyId(): Flow<Long> = currentPropertyIdFlow.onEach {
        android.util.Log.d("Nino", "CurrentPropertyIdRepositoryImplementation.getCurrentPropertyId().onEach called with $it")
    }

    override fun setCurrentPropertyId(currentId: Long) {
        val success = currentPropertyIdFlow.tryEmit(currentId)

        android.util.Log.d(
            "Nino",
            "CurrentPropertyIdRepositoryImplementation.setCurrentPropertyId() called with: currentId = $currentId, success = $success")
    }
}
