package com.bakjoul.realestatemanager.ui.drafts

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback

data class DraftsItemViewState(
    val id: Long,
    val photoUrl: String,
    val lastUpdate: String,
    val typeAndLocation: String,
    val overview: String,
    val description: String,
    val onDraftItemClicked: EquatableCallback
)
