package com.bakjoul.realestatemanager.data.camera

import com.bakjoul.realestatemanager.domain.camera.CameraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRepositoryImplementation @Inject constructor() : CameraRepository {

    private val capturedPhotoUriMutableSharedFlow: MutableSharedFlow<String> = MutableSharedFlow()

    override fun setCapturedPhotoUri(uri: String) {
        capturedPhotoUriMutableSharedFlow.tryEmit(uri)
    }

    override fun getCapturedPhotoUri(): Flow<String> = capturedPhotoUriMutableSharedFlow.asSharedFlow()
}
