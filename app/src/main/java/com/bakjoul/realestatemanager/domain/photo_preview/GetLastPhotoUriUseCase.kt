package com.bakjoul.realestatemanager.domain.photo_preview

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLastPhotoUriUseCase @Inject constructor(private val photoPreviewRepository: PhotoPreviewRepository) {
    fun invoke(): Flow<String> = photoPreviewRepository.getLastPhotoUriFlowAsState()
}
