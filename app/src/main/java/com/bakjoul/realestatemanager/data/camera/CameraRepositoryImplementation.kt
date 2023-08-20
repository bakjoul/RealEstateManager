package com.bakjoul.realestatemanager.data.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import com.bakjoul.realestatemanager.domain.camera.CameraRepository
import com.bakjoul.realestatemanager.ui.camera.activity.CameraActivityViewAction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRepositoryImplementation @Inject constructor(@ApplicationContext private val context: Context) : CameraRepository {

    private val capturedPhotoUriMutableSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
    private val cameraViewActionMutableSharedFlow: MutableSharedFlow<CameraActivityViewAction> = MutableSharedFlow(replay = 1)

    override fun setCapturedPhotoUri(uri: String) {
        capturedPhotoUriMutableSharedFlow.tryEmit(uri)
    }

    override fun getCapturedPhotoUriFlowAsState(): Flow<String> = capturedPhotoUriMutableSharedFlow.asSharedFlow()

    override fun setCameraViewAction(viewAction: CameraActivityViewAction) {
        cameraViewActionMutableSharedFlow.tryEmit(viewAction)
        Log.d("test", "setCameraViewAction: $viewAction")
    }

    override fun getViewActionFlow(): Flow<CameraActivityViewAction> = cameraViewActionMutableSharedFlow.asSharedFlow()

    override fun deleteCapturedPhoto(photoUri: Uri) {
        context.contentResolver.delete(photoUri, null, null)
    }
}
