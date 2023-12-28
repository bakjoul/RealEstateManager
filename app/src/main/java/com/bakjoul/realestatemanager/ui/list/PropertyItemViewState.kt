package com.bakjoul.realestatemanager.ui.list

import android.text.SpannableString
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.NativeText

data class PropertyItemViewState(
    val id: Long,
    val photoUrl: String,
    val type: NativeText,
    val city: String,
    val price: String,
    val currencyRate: SpannableString,
    val features: NativeText,
    val isSold: Boolean,
    val onPropertyClicked: EquatableCallback
)
