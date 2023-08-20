package com.bakjoul.realestatemanager.domain.camera

import com.bakjoul.realestatemanager.ui.camera.activity.CameraActivityViewAction
import javax.inject.Inject

class SetCameraViewActionUseCase @Inject constructor(private val cameraRepository: CameraRepository) {
    fun invoke(viewAction: CameraActivityViewAction) = cameraRepository.setCameraViewAction(viewAction)
}
