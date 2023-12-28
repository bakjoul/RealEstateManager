package com.bakjoul.realestatemanager.data.settings.model

import androidx.annotation.StringRes
import com.bakjoul.realestatemanager.R

enum class SurfaceUnit (@StringRes val unitName: Int, @StringRes val unitSymbol: Int) {
    FEET(R.string.surface_feet_name, R.string.surface_feet_unit),
    METERS(R.string.surface_meters_name, R.string.surface_meters_unit)
}
