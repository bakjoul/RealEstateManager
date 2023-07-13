package com.bakjoul.realestatemanager.ui.utils

import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.ceil

class ViewModelUtils {
    companion object {

        fun formatSurface(surface: Int, surfaceUnit: SurfaceUnit): String {
            return when (surfaceUnit) {
                SurfaceUnit.Meters -> {
                    "$surface ${surfaceUnit.unit}"
                }

                SurfaceUnit.Feet -> {
                    "${ceil(surface.toDouble() * 3.28084).toInt()} ${surfaceUnit.unit}"
                }
            }
        }

        fun formatPrice(price: Double, currency: AppCurrency, euroRate: Double): String {
            val numberFormat = NumberFormat.getNumberInstance()

            val formattedPrice = when (currency) {
                AppCurrency.USD -> {
                    numberFormat.currency = Currency.getInstance(Locale.US)
                    numberFormat.maximumFractionDigits = 0
                    "$" + numberFormat.format(price)
                }

                AppCurrency.EUR -> {
                    val convertedPrice = price / euroRate
                    numberFormat.currency = Currency.getInstance(Locale.FRANCE)
                    numberFormat.maximumFractionDigits = 0
                    numberFormat.format(convertedPrice).replace(",", ".") + " €"
                }
            }

            return formattedPrice
        }
    }
}