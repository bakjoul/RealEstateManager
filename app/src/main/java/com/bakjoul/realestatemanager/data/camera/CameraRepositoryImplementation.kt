package com.bakjoul.realestatemanager.data.camera

import com.bakjoul.realestatemanager.domain.camera.CameraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRepositoryImplementation @Inject constructor() : CameraRepository {

    private val capturedPhotoUriMutableSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
    private val shouldShowPhotoPreviewMutableStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override fun setCapturedPhotoUri(uri: String) {
        capturedPhotoUriMutableSharedFlow.tryEmit(uri)
    }

    override fun getCapturedPhotoUriFlowAsState(): Flow<String> = capturedPhotoUriMutableSharedFlow

    override fun setShouldShowPhotoPreview(shouldShowPhotoPreview: Boolean) {
        shouldShowPhotoPreviewMutableStateFlow.value = shouldShowPhotoPreview
    }

    override fun shouldShowPhotoPreviewFlow(): Flow<Boolean> = shouldShowPhotoPreviewMutableStateFlow.asStateFlow()
}
