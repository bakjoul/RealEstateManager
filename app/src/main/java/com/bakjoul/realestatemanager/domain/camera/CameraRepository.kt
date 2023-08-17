package com.bakjoul.realestatemanager.domain.camera

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface CameraRepository {

    fun setCapturedPhotoUri(uri: Uri)

    fun getCapturedPhotoUri(): Flow<Uri?>
}