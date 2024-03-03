package com.bakjoul.realestatemanager.designsystem.molecule.photo_list

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.EquatableCallbackWithTwoParams

data class PhotoListItemViewState(
    val id: Int,
    val photoId: Long,
    val uri: String,
    val description: String,
    val selectType: SelectType,
    val isFeatured: Boolean?,
    val onPhotoClicked: EquatableCallback,
    val onFeaturePhotoClicked: EquatableCallback?,
    val onDeletePhotoClicked: EquatableCallbackWithTwoParams<Long, String>?,
    val onDescriptionClicked: EquatableCallbackWithTwoParams<Long, String>?
)

 enum class SelectType {
     SELECTED,
     NOT_SELECTED,
     NOT_SELECTABLE
 }
