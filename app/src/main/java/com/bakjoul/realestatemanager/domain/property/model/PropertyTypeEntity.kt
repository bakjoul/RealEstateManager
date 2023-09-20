package com.bakjoul.realestatemanager.domain.property.model

import androidx.annotation.StringRes
import com.bakjoul.realestatemanager.R

enum class PropertyTypeEntity(@StringRes val stringRes: Int) {
    DUPLEX(R.string.property_type_duplex),
    FLAT(R.string.property_type_flat),
    HOUSE(R.string.property_type_house),
    LOFT(R.string.property_type_loft),
    OTHER(R.string.property_type_other),
    PENTHOUSE(R.string.property_type_penthouse)
}
