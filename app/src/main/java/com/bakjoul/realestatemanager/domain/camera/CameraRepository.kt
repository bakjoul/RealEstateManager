package com.bakjoul.realestatemanager.domain.camera

import android.net.Uri
import com.bakjoul.realestatemanager.ui.camera.activity.CameraActivityViewAction
import kotlinx.coroutines.flow.Flow

interface CameraRepository {

    fun setCapturedPhotoUri(uri: String)

    fun getCapturedPhotoUriFlowAsState(): Flow<String>

    fun setCameraViewAction(viewAction: CameraActivityViewAction)

    fun getViewActionFlow(): Flow<CameraActivityViewAction>

    fun deleteCapturedPhoto(photoUri: Uri)
}
