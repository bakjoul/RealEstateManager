package com.bakjoul.realestatemanager.ui.photos

import com.bakjoul.realestatemanager.ui.details.DetailsMediaItemViewState

data class PhotosViewState(
    val photosUrls: List<String>,
    val thumbnails: List<DetailsMediaItemViewState>,
    val currentPhotoId: Int
)