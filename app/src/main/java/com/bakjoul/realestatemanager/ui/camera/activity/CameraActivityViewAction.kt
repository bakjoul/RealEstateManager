package com.bakjoul.realestatemanager.ui.camera.activity

sealed class CameraActivityViewAction {
    object ShowPhotoPreview : CameraActivityViewAction()
    object ClosePhotoPreview : CameraActivityViewAction()
    object CloseCamera : CameraActivityViewAction()
}
