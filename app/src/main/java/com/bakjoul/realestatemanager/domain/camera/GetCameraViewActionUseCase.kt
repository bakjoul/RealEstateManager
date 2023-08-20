package com.bakjoul.realestatemanager.domain.camera

import com.bakjoul.realestatemanager.ui.camera.activity.CameraActivityViewAction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCameraViewActionUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    fun invoke(): Flow<CameraActivityViewAction> = cameraRepository.getViewActionFlow()
}
