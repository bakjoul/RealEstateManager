package com.bakjoul.realestatemanager.domain.current_photo

import kotlinx.coroutines.flow.Flow

interface CurrentPhotoIdRepository {

    fun setCurrentPhotoId(currentId: Int)

    fun getCurrentPhotoIdFlow(): Flow<Int>
}
