package com.bakjoul.realestatemanager.ui.common

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback

data class SuggestionItemViewState(
    val id: String,
    val description: String,
    val onSuggestionClicked: EquatableCallback
)
