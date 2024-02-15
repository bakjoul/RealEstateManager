package com.bakjoul.realestatemanager.ui.camera.photo_preview

import com.bakjoul.realestatemanager.ui.utils.NativeText

sealed class PhotoPreviewViewAction {
    data class ShowToast(val message: NativeText) : PhotoPreviewViewAction()
}
