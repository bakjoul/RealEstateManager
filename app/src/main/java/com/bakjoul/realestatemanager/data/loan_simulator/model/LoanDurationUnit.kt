package com.bakjoul.realestatemanager.data.loan_simulator.model

import androidx.annotation.StringRes
import com.bakjoul.realestatemanager.R

enum class LoanDurationUnit(@StringRes val unitName: Int) {
    YEARS(R.string.duration_unit_years),
    MONTHS(R.string.duration_unit_months),
}
