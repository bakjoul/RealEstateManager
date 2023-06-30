package com.bakjoul.realestatemanager.domain.current_photo

import kotlinx.coroutines.flow.Flow

interface CurrentPhotoIdRepository {

    fun getCurrentPhotoIdFlow(): Flow<Int>

    fun setCurrentPhotoId(currentId: Int)

    fun resetCurrentPhotoId()
}
