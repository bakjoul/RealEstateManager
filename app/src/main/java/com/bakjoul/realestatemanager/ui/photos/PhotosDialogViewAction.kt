package com.bakjoul.realestatemanager.ui.photos

sealed class PhotosDialogViewAction {
    object ShowPhotosDialog : PhotosDialogViewAction()
    object ClosePhotosDialog : PhotosDialogViewAction()
}
