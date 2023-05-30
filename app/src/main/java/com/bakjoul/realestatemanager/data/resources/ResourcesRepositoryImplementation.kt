package com.bakjoul.realestatemanager.data.resources

import com.bakjoul.realestatemanager.domain.resources.ResourcesRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourcesRepositoryImplementation @Inject constructor() : ResourcesRepository {

    private val isTabletMutableStateFlow = MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun isTabletFlow(): Flow<Boolean> = isTabletMutableStateFlow

    override fun setOrientation(isTablet: Boolean) {
        isTabletMutableStateFlow.tryEmit(isTablet)
    }
}