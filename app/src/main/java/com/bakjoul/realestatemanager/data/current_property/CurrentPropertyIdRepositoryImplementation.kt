package com.bakjoul.realestatemanager.data.current_property

import android.util.Log
import com.bakjoul.realestatemanager.domain.current_property.CurrentPropertyIdRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPropertyIdRepositoryImplementation @Inject constructor() : CurrentPropertyIdRepository {

    private val currentPropertyIdChannel = Channel<Long>().apply {
        Log.d("test", getCurrentPropertyId().toString())
    }

    override fun getCurrentPropertyId(): Flow<Long> = currentPropertyIdChannel.receiveAsFlow()

    override fun setCurrentPropertyId(currentId: Long) {
        currentPropertyIdChannel.trySend(currentId)
        Log.d("test", "setCurrentPropertyId: $currentId")
    }
}
