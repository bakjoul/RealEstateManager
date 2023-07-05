package com.bakjoul.realestatemanager.data.current_photo

import com.bakjoul.realestatemanager.domain.current_photo.CurrentPhotoIdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentPhotoIdRepositoryImplementation @Inject constructor() : CurrentPhotoIdRepository {

    private val currentPhotoIdMutableStateFlow: MutableStateFlow<Int> = MutableStateFlow(-1)

    override fun getCurrentPhotoIdFlow(): Flow<Int> = currentPhotoIdMutableStateFlow.asStateFlow()

    override fun setCurrentPhotoId(currentId: Int) {
        if (currentPhotoIdMutableStateFlow.value != currentId) {
            currentPhotoIdMutableStateFlow.value = currentId
        }
    }

    override fun resetCurrentPhotoId() {
        if (currentPhotoIdMutableStateFlow.value != -1) {
            currentPhotoIdMutableStateFlow.value = -1
        }
    }
}
