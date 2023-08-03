package com.bakjoul.realestatemanager.ui.list

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.currency_rate.GetCachedEuroRateUseCase
import com.bakjoul.realestatemanager.domain.current_property.SetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.property.GetPropertiesFlowUseCase
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatPrice
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatSurface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    private val getPropertiesFlowUseCase: GetPropertiesFlowUseCase,
    private val setCurrentPropertyIdUseCase: SetCurrentPropertyIdUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getEuroRateUseCase: GetCachedEuroRateUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val isTabletUseCase: IsTabletUseCase
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val propertiesLiveData: LiveData<List<PropertyItemViewState>> = liveData {
        combine(
            getPropertiesFlowUseCase.invoke(),
            getCurrentCurrencyUseCase.invoke(),
            getEuroRateUseCase.invoke(),
            getCurrentSurfaceUnitUseCase.invoke(),
            isTabletUseCase.invoke()
        ) { properties, currency, euroRate, surfaceUnit, isTablet ->
            properties.map {
                PropertyItemViewState(
                    id = it.id,
                    photoUrl = it.photos.firstOrNull()?.url ?: "",
                    type = it.type,
                    city = it.city,
                    features = formatFeatures(it.bedrooms, it.bathrooms, it.surface, surfaceUnit, isTablet),
                    price = formatPrice(it.price, currency, euroRate.rate),
                    currencyRate = formatRate(currency, euroRate.rate, euroRate.updateDate.format(dateFormatter)),
                    isSold = it.soldDate != null,
                    onPropertyClicked = EquatableCallback { setCurrentPropertyIdUseCase.invoke(it.id) }
                )
            }
        }.collect { propertiesItemViewStates ->
            emit(propertiesItemViewStates)
        }
    }

    private fun formatFeatures(bedrooms: Int, bathrooms: Int, surface: Double, surfaceUnit: SurfaceUnit, isTablet: Boolean): String {
        if (isTablet) {
            val builder = StringBuilder()
            builder.append(bedrooms)
            if (bedrooms > 0) {
                builder.append(" bedrooms")
            } else {
                builder.append(" bedroom")
            }
            builder.append(" - ")
            builder.append(bathrooms)
            if (bathrooms > 0) {
                builder.append(" bathrooms")
            } else {
                builder.append(" bathroom")
            }
            builder.append(" - ")
            builder.append(" ${formatSurface(surface, surfaceUnit)}")

            return builder.toString()
        } else {
            return "$bedrooms bed. - $bathrooms bath. - ${formatSurface(surface, surfaceUnit)}"
        }
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
