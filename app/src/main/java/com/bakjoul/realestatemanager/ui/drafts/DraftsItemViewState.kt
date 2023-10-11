package com.bakjoul.realestatemanager.ui.drafts

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback

data class DraftsItemViewState(
    val id: Long,
    val entryDate: String,
    val onDraftItemClicked: EquatableCallback
)
