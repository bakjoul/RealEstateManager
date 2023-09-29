package com.bakjoul.realestatemanager.ui.utils

import android.util.Log
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.ceil

class ViewModelUtils {

    companion object {
        private var viewModelCounter = 0

        fun formatSurface(surface: Double, surfaceUnit: SurfaceUnit): Pair<Int, String> = when (surfaceUnit) {
            SurfaceUnit.Meters -> surface.toInt() to surfaceUnit.unit
            SurfaceUnit.Feet -> ceil(surface * 3.28084).toInt() to surfaceUnit.unit
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

        fun generateViewModelKey(): String {
            Log.d("test", "generateViewModelKey: $viewModelCounter")
            viewModelCounter++
            return "PlusMinusViewModel$viewModelCounter"
        }
    }
}
