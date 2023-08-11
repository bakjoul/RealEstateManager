package com.bakjoul.realestatemanager.ui.list

import android.app.Application
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.currency_rate.GetEuroRateUseCase
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
import kotlinx.coroutines.flow.flow
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    private val application: Application,
    private val getPropertiesFlowUseCase: GetPropertiesFlowUseCase,
    private val setCurrentPropertyIdUseCase: SetCurrentPropertyIdUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getEuroRateUseCase: GetEuroRateUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val isTabletUseCase: IsTabletUseCase
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val propertiesLiveData: LiveData<List<PropertyItemViewState>> = liveData {
        combine(
            getPropertiesFlowUseCase.invoke(),
            getCurrentCurrencyUseCase.invoke(),
            flow { emit(getEuroRateUseCase.invoke()) },
            getCurrentSurfaceUnitUseCase.invoke(),
            isTabletUseCase.invoke(),
        ) { properties, currency, euroRateWrapper, surfaceUnit, isTablet ->
            properties.map {
                PropertyItemViewState(
                    id = it.id,
                    photoUrl = it.photos.firstOrNull()?.url ?: "",
                    type = it.type,
                    city = it.city,
                    features = formatFeatures(it.bedrooms, it.bathrooms, it.surface, surfaceUnit, isTablet),
                    price = formatPrice(it.price, currency, euroRateWrapper.currencyRateEntity.rate),
                    currencyRate = formatRate(
                        currency,
                        euroRateWrapper.currencyRateEntity.rate,
                        euroRateWrapper.currencyRateEntity.updateDate.format(dateFormatter)
                    ),
                    isSold = it.soldDate != null,
                    onPropertyClicked = EquatableCallback { setCurrentPropertyIdUseCase.invoke(it.id) }
                )
            }
        }.collect { propertiesItemViewStates ->
            emit(propertiesItemViewStates)
        }
    }

    private fun formatFeatures(bedrooms: Int, bathrooms: Int, surface: Double, surfaceUnit: SurfaceUnit, isTablet: Boolean): String {
        val (mappedSurface, mappedSurfaceUnit) = formatSurface(surface, surfaceUnit)

        return if (isTablet) {
            application.resources.getString(
                R.string.features,
                application.resources.getQuantityString(R.plurals.bedroom_plural, bedrooms, bedrooms),
                application.resources.getQuantityString(R.plurals.bathroom_plural, bathrooms, bathrooms),
                application.resources.getString(R.string.surface, mappedSurface, mappedSurfaceUnit)
            )
        } else {
            String.format("%d bed. - %d bath. - %d %s", bedrooms, bathrooms, mappedSurface, mappedSurfaceUnit)
        }
    }

    private fun formatRate(currency: AppCurrency, euroRate: Double, updateDate: String): SpannableString {
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
