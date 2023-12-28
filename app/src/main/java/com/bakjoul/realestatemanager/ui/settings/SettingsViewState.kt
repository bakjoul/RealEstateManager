package com.bakjoul.realestatemanager.ui.settings

import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit

data class SettingsViewState (
    val currency: AppCurrency,
    val surfaceUnit: SurfaceUnit,
)
