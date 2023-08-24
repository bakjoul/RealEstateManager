package com.bakjoul.realestatemanager.domain.navigation.model

sealed class To {
    object Preview : To()
    object CloseCamera : To()
    object ClosePhotoPreview : To()
}