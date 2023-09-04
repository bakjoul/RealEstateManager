package com.bakjoul.realestatemanager.ui.add

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback

data class AddPropertyPhotoItemViewState (
    val id: Long,
    val url: String,
    val description: String,
    val onPhotoClicked: EquatableCallback,
    val onDeletePhotoClicked: EquatableCallback
)
