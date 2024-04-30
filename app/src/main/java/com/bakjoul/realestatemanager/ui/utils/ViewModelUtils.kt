package com.bakjoul.realestatemanager.ui.utils

import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ViewModelUtils {

    companion object {

        fun formatSurfaceLabel(surfaceUnit: SurfaceUnit): NativeText {
            return when (surfaceUnit) {
                SurfaceUnit.FEET -> {
                    NativeText.Multi(
                        listOf(
                            NativeText.Argument(
                                R.string.add_property_label_surface,
                                NativeText.Resource(surfaceUnit.unitSymbol)
                            ),
                            NativeText.Simple("*")
                        )
                    )
                }

                else -> {
                    NativeText.Argument(
                        R.string.add_property_label_surface,
                        NativeText.Resource(surfaceUnit.unitSymbol)
                    )
                }
            }
        }

        fun formatSurfaceValue(surface: BigDecimal, surfaceUnit: SurfaceUnit): Int = when (surfaceUnit) {
            SurfaceUnit.METERS -> surface.setScale(0, RoundingMode.CEILING).toInt()
            SurfaceUnit.FEET -> (surface * BigDecimal(3.28084)).setScale(0, RoundingMode.CEILING).toInt()
        }

        fun formatPriceHint(currency: AppCurrency): NativeText {
            return NativeText.Argument(
                R.string.add_property_price_hint,
                NativeText.Resource(currency.currencySymbol)
            )
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

        fun getCurrencyFormat(currency: AppCurrency): DecimalFormat {
            val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                groupingSeparator = if (currency == AppCurrency.EUR) {
                    ' '
                } else {
                    ','
                }
                decimalSeparator = if (currency == AppCurrency.EUR) {
                    ','
                } else {
                    '.'
                }
            }

            return DecimalFormat("#,###.##", symbols)
        }
    }
}
