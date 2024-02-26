package com.bakjoul.realestatemanager.domain.photo_preview

import kotlinx.coroutines.flow.Flow

interface PhotoPreviewRepository {

    fun setLastPhotoUri(photoUri: String)

    fun getLastPhotoUriFlowAsState(): Flow<String>
}
