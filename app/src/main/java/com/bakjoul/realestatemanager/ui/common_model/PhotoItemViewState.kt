package com.bakjoul.realestatemanager.ui.common_model

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback

data class PhotoItemViewState(
    val id: Long,
    val url: String,
    val description: String,
    val onPhotoClicked: EquatableCallback
)
