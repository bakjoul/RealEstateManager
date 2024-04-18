package com.bakjoul.realestatemanager.data.search.model

import androidx.annotation.StringRes
import com.bakjoul.realestatemanager.R

enum class SearchDurationUnit(@StringRes val unitName: Int) {
    WEEKS(R.string.duration_unit_weeks),
    MONTHS(R.string.duration_unit_months),
    YEARS(R.string.duration_unit_years)
}
