package com.bakjoul.realestatemanager.ui.camera.activity

sealed class CameraActivityViewAction {
    data class ShowPhotoPreview(val propertyId: Long, val isExistingProperty: Boolean) : CameraActivityViewAction()
    object ClosePhotoPreview : CameraActivityViewAction()
    object CloseCamera : CameraActivityViewAction()
}
