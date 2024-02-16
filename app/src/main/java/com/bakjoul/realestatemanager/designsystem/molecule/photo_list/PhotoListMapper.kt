package com.bakjoul.realestatemanager.designsystem.molecule.photo_list

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback

class PhotoListMapper {
    fun map(
        photos: List<PhotoEntity>,
        selectType: (Int) -> SelectType,
        featuredPhotoId: Long?,
        onPhotoClicked: ((Int) -> Unit),
        onFeatureClicked: ((Long) -> Unit)? = null,
        onDeleteClicked: ((Long) -> Unit)? = null) : List<PhotoListItemViewState> = photos.mapIndexed { index, photo ->
        PhotoListItemViewState(
            id = photo.id,
            url = photo.url,
            description = photo.description,
            selectType = selectType(index),
            isFeatured = featuredPhotoId == photo.id,
            onPhotoClicked = EquatableCallback { onPhotoClicked(index) },
            onFeaturePhotoClicked = if (onFeatureClicked != null) {
                EquatableCallback { onFeatureClicked(photo.id) }
            } else {
                null
            },
            onDeletePhotoClicked = if (onDeleteClicked != null) {
                EquatableCallback { onDeleteClicked(photo.id) }
            } else {
                null
            }
        )
    }
}
