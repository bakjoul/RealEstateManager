package com.bakjoul.realestatemanager.designsystem.molecule.photo_list

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback

data class PhotoListItemViewState(
    val id: Long,
    val url: String,
    val description: String,
    val selectType: SelectType,
    val onPhotoClicked: EquatableCallback,
    val onDeletePhotoClicked: EquatableCallback?
)

 enum class SelectType {
     SELECTED,
     NOT_SELECTED,
     NOT_SELECTABLE
 }
