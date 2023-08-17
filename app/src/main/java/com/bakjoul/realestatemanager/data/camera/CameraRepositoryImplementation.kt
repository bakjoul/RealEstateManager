package com.bakjoul.realestatemanager.data.camera

import android.net.Uri
import com.bakjoul.realestatemanager.domain.camera.CameraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRepositoryImplementation @Inject constructor() : CameraRepository {

    private val capturedPhotoUriMutableStateFlow: MutableStateFlow<Uri?> = MutableStateFlow(null)

    override fun setCapturedPhotoUri(uri: Uri) {
        capturedPhotoUriMutableStateFlow.value = uri
    }

    override fun getCapturedPhotoUri(): Flow<Uri?> = capturedPhotoUriMutableStateFlow.asStateFlow()
}
