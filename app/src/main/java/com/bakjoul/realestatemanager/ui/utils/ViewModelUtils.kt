package com.bakjoul.realestatemanager.ui.utils

import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ViewModelUtils {

    companion object {

        // TODO how to use NativeText here ?
        fun formatSurface(surface: BigDecimal, surfaceUnit: SurfaceUnit): Pair<Int, Int> = when (surfaceUnit) {
            SurfaceUnit.METERS -> surface.setScale(0, RoundingMode.CEILING).toInt() to surfaceUnit.unitSymbol
            SurfaceUnit.FEET -> (surface * BigDecimal(3.28084)).setScale(0, RoundingMode.CEILING).toInt() to surfaceUnit.unitSymbol
        }

        fun formatPrice(price: BigDecimal, currency: AppCurrency, euroRate: Double): String {
            val symbols = DecimalFormatSymbols(Locale.getDefault())
            symbols.groupingSeparator = if (currency == AppCurrency.EUR) ' ' else ','
            symbols.decimalSeparator = if (currency == AppCurrency.EUR) ',' else '.'
            val decimalFormat = DecimalFormat("#,###.##", symbols)

            val formattedPrice = when (currency) {
                AppCurrency.USD -> "$" + decimalFormat.format(price)
                AppCurrency.EUR -> {
                    val convertedPrice = price / euroRate.toBigDecimal()
                    decimalFormat.format(convertedPrice) + "€"
                }
            }
            return formattedPrice
        }
    }
}
