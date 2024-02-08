package com.bakjoul.realestatemanager.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ViewModelTestUtils {

    companion object {
        fun getCurrencyFormat(isCurrencyEuro: Boolean = false): DecimalFormat {
            val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                groupingSeparator = if (isCurrencyEuro) {
                    ' '
                } else {
                    ','
                }
                decimalSeparator = if (isCurrencyEuro) {
                    ','
                } else {
                    '.'
                }
            }

            return DecimalFormat("#,###.##", symbols)
        }
    }
}