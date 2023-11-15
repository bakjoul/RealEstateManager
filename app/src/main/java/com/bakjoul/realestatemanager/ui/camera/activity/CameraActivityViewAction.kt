package com.bakjoul.realestatemanager.ui.camera.activity

sealed class CameraActivityViewAction {
    data class ShowPhotoPreview(val propertyId: Long) : CameraActivityViewAction()
    object ClosePhotoPreview : CameraActivityViewAction()
    object CloseCamera : CameraActivityViewAction()
}
