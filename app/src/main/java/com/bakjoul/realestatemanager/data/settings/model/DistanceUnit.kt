package com.bakjoul.realestatemanager.data.settings.model

import androidx.annotation.StringRes
import com.bakjoul.realestatemanager.R

enum class DistanceUnit (@StringRes val unitName: Int, @StringRes val unitSymbol: Int) {
    KILOMETERS(R.string.distance_kilometers_name, R.string.distance_kilometers_unit),
    MILES(R.string.distance_miles_name, R.string.distance_miles_unit)
}
