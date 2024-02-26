package com.bakjoul.realestatemanager.data.photo_preview

import com.bakjoul.realestatemanager.domain.photo_preview.PhotoPreviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoPreviewRepositoryImplementation @Inject constructor() : PhotoPreviewRepository {

    private val lastPhotoUriMutableSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)

    override fun setLastPhotoUri(photoUri: String) {
        lastPhotoUriMutableSharedFlow.tryEmit(photoUri)
    }

    override fun getLastPhotoUriFlowAsState(): Flow<String> = lastPhotoUriMutableSharedFlow.asSharedFlow()
}
