package com.bakjoul.realestatemanager.ui.camera.photo_preview

import android.net.Uri

data class PhotoPreviewViewState (
    val photoUri: Uri?,
    val descriptionError: String?
)
