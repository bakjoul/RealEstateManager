package com.bakjoul.realestatemanager.data.current_photo

import android.util.Log
import com.bakjoul.realestatemanager.domain.current_photo.CurrentPhotoIdRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPhotoIdRepositoryImplementation @Inject constructor() : CurrentPhotoIdRepository {

    private val currentPhotoIdMutableStateFlow: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val currentPhotoIdChannel: Channel<Int> = Channel()

    override fun getCurrentPhotoIdChannel(): Channel<Int> = currentPhotoIdChannel

    override fun getCurrentPhotoIdFlow(): Flow<Int> = currentPhotoIdMutableStateFlow.filterNotNull()

    override fun setCurrentPhotoId(currentId: Int) {
        currentPhotoIdMutableStateFlow.value = currentId
        currentPhotoIdChannel.trySend(currentId)
    }
}
