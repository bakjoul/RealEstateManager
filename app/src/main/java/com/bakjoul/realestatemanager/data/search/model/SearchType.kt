package com.bakjoul.realestatemanager.data.search.model

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.bakjoul.realestatemanager.R

enum class SearchType(@StringRes val typeName: Int, @IdRes val chipId: Int) {
    DUPLEX(R.string.property_type_duplex, R.id.search_type_duplex_Chip),
    FLAT(R.string.property_type_flat, R.id.search_type_flat_Chip),
    HOUSE(R.string.property_type_house, R.id.search_type_house_Chip),
    LOFT(R.string.property_type_loft, R.id.search_type_loft_Chip),
    OTHER(R.string.property_type_other, R.id.search_type_other_Chip),
    PENTHOUSE(R.string.property_type_penthouse, R.id.search_type_penthouse_Chip),
}
