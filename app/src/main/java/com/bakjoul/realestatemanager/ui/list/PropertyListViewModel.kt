package com.bakjoul.realestatemanager.ui.list

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.domain.currency_rate.GetCachedEuroRateUseCase
import com.bakjoul.realestatemanager.domain.current_property.SetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.property.GetPropertiesFlowUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    private val getPropertiesFlowUseCase: GetPropertiesFlowUseCase,
    private val setCurrentPropertyIdUseCase: SetCurrentPropertyIdUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getCachedEuroRateUseCase: GetCachedEuroRateUseCase
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val propertiesLiveData: LiveData<List<PropertyItemViewState>> = liveData {
        combine(
            getPropertiesFlowUseCase.invoke(),
            getCurrentCurrencyUseCase.invoke(),
            getCachedEuroRateUseCase.invoke()
        ) { properties, currency, euroRate ->
            properties.map {
                PropertyItemViewState(
                    id = it.id,
                    photoUrl = it.photos.firstOrNull()?.url ?: "",
                    type = it.type,
                    city = it.city,
                    features = formatFeatures(it.bedrooms, it.bathrooms, it.surface),
                    price = formatPrice(it.price, currency, euroRate.rate),
                    currencyRate = formatRate(currency, euroRate.rate, euroRate.updateDate.format(dateFormatter)),
                    onPropertyClicked = EquatableCallback { setCurrentPropertyIdUseCase.invoke(it.id) }
                )
            }
        }.collect { propertiesItemViewStates ->
            emit(propertiesItemViewStates)
        }
    }

    private fun formatFeatures(bedrooms: Int, bathrooms: Int, surface: Int): String {
        return "$bedrooms bed. - $bathrooms bath. - $surface sqm"
    }

    private fun formatPrice(price: Double, currency: AppCurrency, euroRate: Double): String {
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

    private fun formatRate(currency: AppCurrency, euroRate: Double, updateDate: String): SpannableString  {
        val rateText = "\$$euroRate"
        val formattedDate = formatDateString(updateDate)
        val fullText = when (currency) {
            AppCurrency.EUR -> "Exchange rate: $rateText\nUpdated on $formattedDate"
            else -> ""
        }

        val spannableBuilder = SpannableStringBuilder(fullText)

        if (rateText.isNotEmpty() && fullText.contains(rateText)) {
            val startEuroRate = fullText.indexOf(rateText)
            val endEuroRate = startEuroRate + rateText.length
            spannableBuilder.setSpan(StyleSpan(Typeface.BOLD), startEuroRate, endEuroRate, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return SpannableString.valueOf(spannableBuilder)
    }

    private fun formatDateString(dateString: String): String? {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date: Date? = try {
            inputFormat.parse(dateString)
        } catch (e: ParseException) {
            null
        }

        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        return date?.let { outputFormat.format(it) }
    }
}
