package com.bakjoul.realestatemanager.domain.current_photo

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

interface CurrentPhotoIdRepository {

    fun getCurrentPhotoIdFlow(): Flow<Int>

    fun getCurrentPhotoIdChannel(): Channel<Int>

    fun setCurrentPhotoId(currentId: Int)
}
