package com.bakjoul.realestatemanager.ui.details

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback

data class DetailsMediaItemViewState(
    val id: Long,
    val url: String,
    val description: String,
    val onPhotoClicked: EquatableCallback
)
