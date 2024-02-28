package com.bakjoul.realestatemanager.designsystem.molecule.photo_list

import com.bakjoul.realestatemanager.domain.photos.model.PhotoEntity
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.EquatableCallbackWithTwoParams

class PhotoListMapper {
    fun map(
        photos: List<PhotoEntity>,
        selectType: (Int) -> SelectType,
        featuredPhotoId: Long?,
        onPhotoClicked: ((Int) -> Unit),
        onFeatureClicked: ((Long) -> Unit)? = null,
        onDeleteClicked: ((Long, String) -> Unit)? = null,
        onDescriptionClicked: ((Long, String) -> Unit)? = null
    ) : List<PhotoListItemViewState> = photos.mapIndexed { index, photo ->
        PhotoListItemViewState(
            id = photo.id,
            uri = photo.uri,
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
                EquatableCallbackWithTwoParams { id, uri ->
                    onDeleteClicked(id, uri)
                }
            } else {
                null
            },
            onDescriptionClicked = if (onDescriptionClicked != null) {
                EquatableCallbackWithTwoParams { id, description ->
                    onDescriptionClicked(id, description)
                }
            } else {
                null
            }
        )
    }
}
