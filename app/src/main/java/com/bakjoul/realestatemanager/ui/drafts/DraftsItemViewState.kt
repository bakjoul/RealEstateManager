package com.bakjoul.realestatemanager.ui.drafts

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.NativeText

data class DraftsItemViewState(
    val id: Long,
    val featuredPhotoUrl: String,
    val isSold: Boolean,
    val lastUpdate: NativeText,
    val typeAndLocation: NativeText,
    val overview: NativeText,
    val description: NativeText,
    val onDraftItemClicked: EquatableCallback
)
