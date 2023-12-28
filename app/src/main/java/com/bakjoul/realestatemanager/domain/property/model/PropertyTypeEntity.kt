package com.bakjoul.realestatemanager.domain.property.model

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.bakjoul.realestatemanager.R

enum class PropertyTypeEntity(@StringRes val typeName: Int, @IdRes val radioButtonId: Int) {
    DUPLEX(R.string.property_type_duplex, R.id.add_property_type_duplex_RadioButton),
    FLAT(R.string.property_type_flat, R.id.add_property_type_flat_RadioButton),
    HOUSE(R.string.property_type_house, R.id.add_property_type_house_RadioButton),
    LOFT(R.string.property_type_loft, R.id.add_property_type_loft_RadioButton),
    OTHER(R.string.property_type_other, R.id.add_property_type_other_RadioButton),
    PENTHOUSE(R.string.property_type_penthouse, R.id.add_property_type_penthouse_RadioButton),
}
