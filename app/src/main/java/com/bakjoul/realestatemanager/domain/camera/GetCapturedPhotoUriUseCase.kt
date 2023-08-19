package com.bakjoul.realestatemanager.domain.camera

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCapturedPhotoUriUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    fun invoke(): Flow<String> = cameraRepository.getCapturedPhotoUriFlowAsState()
}
