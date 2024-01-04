package com.bakjoul.realestatemanager.ui.drafts

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.NativeText

data class DraftsItemViewState(
    val id: Long,
    val photoUrl: String,
    val lastUpdate: String,
    val typeAndLocation: String,
    val overview: NativeText,
    val description: String,
    val onDraftItemClicked: EquatableCallback
)
