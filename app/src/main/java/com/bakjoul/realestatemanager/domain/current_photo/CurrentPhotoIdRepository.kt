package com.bakjoul.realestatemanager.domain.current_photo

import kotlinx.coroutines.flow.Flow

interface CurrentPhotoIdRepository {

    fun getCurrentPhotoIdFlowAsState(): Flow<Int>

    fun getCurrentPhotoIdFlowAsEvent(): Flow<Int>

    fun setCurrentPhotoId(currentId: Int)
}
