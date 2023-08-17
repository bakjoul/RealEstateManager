package com.bakjoul.realestatemanager.domain.camera

import kotlinx.coroutines.flow.Flow

interface CameraRepository {

    fun setCapturedPhotoUri(uri: String)

    fun getCapturedPhotoUri(): Flow<String>
}