package com.bakjoul.realestatemanager.ui.photos

import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListItemViewState

data class PhotosViewState(
    val photosUrls: List<String>,
    val thumbnails: List<PhotoListItemViewState>,
    val currentPhotoId: Int
)
