package com.bakjoul.realestatemanager.data.current_photo

import com.bakjoul.realestatemanager.domain.current_photo.CurrentPhotoIdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPhotoIdRepositoryImplementation @Inject constructor() : CurrentPhotoIdRepository {

    private val currentPhotoIdMutableSharedFlow: MutableSharedFlow<Int?> = MutableSharedFlow(replay = 1)

    override fun setCurrentPhotoId(currentId: Int) {
        currentPhotoIdMutableSharedFlow.tryEmit(currentId)
    }

    override fun getCurrentPhotoIdFlow(): Flow<Int> = currentPhotoIdMutableSharedFlow.asSharedFlow().filterNotNull()
}
