package com.bakjoul.realestatemanager.ui.add

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback

data class AddPropertySuggestionItemViewState(
    val id: String,
    val address: String,
    val onSuggestionClicked: EquatableCallback,
)
