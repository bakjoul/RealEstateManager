package com.bakjoul.realestatemanager.ui.photos

import com.bakjoul.realestatemanager.ui.common_model.PhotoItemViewState

data class PhotosViewState(
    val photosUrls: List<String>,
    val thumbnails: List<PhotoItemViewState>,
    val currentPhotoId: Int
)
