package com.bakjoul.realestatemanager.domain.camera

import kotlinx.coroutines.flow.Flow

interface CameraRepository {

    fun setCapturedPhotoUri(uri: String)

    fun getCapturedPhotoUriFlowAsState(): Flow<String>

    fun setShouldShowPhotoPreview(shouldShowPhotoPreview: Boolean)

    fun shouldShowPhotoPreviewFlow(): Flow<Boolean>
}