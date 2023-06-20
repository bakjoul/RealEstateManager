package com.bakjoul.realestatemanager.ui.list

import com.bakjoul.realestatemanager.ui.utils.EquatableCallback

data class PropertyItemViewState(
    val id: Long,
    val photoUrl: String,
    val type: String,
    val city: String,
    val price: String,
    val currencyRate: String,
    val features: String,
    val onPropertyClicked: EquatableCallback
)
