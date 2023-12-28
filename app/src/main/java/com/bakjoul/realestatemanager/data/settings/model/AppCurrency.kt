package com.bakjoul.realestatemanager.data.settings.model

import androidx.annotation.StringRes
import com.bakjoul.realestatemanager.R

enum class AppCurrency(@StringRes val currencyName: Int, @StringRes val currencySymbol: Int) {
    EUR(R.string.currency_euro_name, R.string.currency_euro_symbol),
    USD(R.string.currency_dollar_name, R.string.currency_dollar_symbol),
}
