package com.bakjoul.realestatemanager.domain.navigation.model

sealed class To {
    object Details: To()
    object CloseDetails: To()
    object PhotosDialog: To()
    object ClosePhotosDialog: To()
    object AddProperty : To()
    object CloseAddProperty : To()
    object HideAddressSuggestions : To()
    object Camera : To()
    object CloseCamera : To()
    object PhotoPreview : To()
    object ClosePhotoPreview : To()
    object Dispatcher: To()
    object Settings : To()
    object CloseSettings : To()
}
