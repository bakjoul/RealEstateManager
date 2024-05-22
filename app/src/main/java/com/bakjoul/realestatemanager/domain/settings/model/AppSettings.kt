package com.bakjoul.realestatemanager.domain.settings.model

import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.DistanceUnit
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit

data class AppSettings(
    val currency: AppCurrency,
    val surfaceUnit: SurfaceUnit,
    val distanceUnit: DistanceUnit,
)
