package com.bakjoul.realestatemanager.data.resources

import com.bakjoul.realestatemanager.domain.resources.ResourcesRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourcesRepositoryImplementation @Inject constructor() : ResourcesRepository {

    private val isTabletMutableSharedFlow = MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun isTabletFlow(): Flow<Boolean> = isTabletMutableSharedFlow.distinctUntilChanged()

    override fun setOrientation(isTablet: Boolean) {
        isTabletMutableSharedFlow.tryEmit(isTablet)
    }
}
