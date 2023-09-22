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
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.currency_rate.GetEuroRateUseCase
import com.bakjoul.realestatemanager.domain.current_property.SetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.property.GetPropertiesFlowUseCase
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
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
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val application: Application,
    private val getPropertiesFlowUseCase: GetPropertiesFlowUseCase,
    private val setCurrentPropertyIdUseCase: SetCurrentPropertyIdUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getEuroRateUseCase: GetEuroRateUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val isTabletUseCase: IsTabletUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val propertiesLiveData: LiveData<List<PropertyItemViewState>> = liveData(coroutineDispatcherProvider.io) {
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
                    photoUrl = it.photos.first().url,
                    type = formatType(it.type),
                    city = it.address.city,
                    features = formatFeatures(it.bedrooms, it.bathrooms, it.surface, surfaceUnit, isTablet),
                    price = formatPrice(it.price, currency, euroRateWrapper.currencyRateEntity.rate),
                    currencyRate = formatRate(currency, euroRateWrapper.currencyRateEntity.rate, euroRateWrapper.currencyRateEntity.updateDate.format(dateFormatter)),
                    isSold = it.saleDate != null,
                    onPropertyClicked = EquatableCallback {
                        setCurrentPropertyIdUseCase.invoke(it.id)
                        navigateUseCase.invoke(To.Details)
                    }
                )
            }
        }.collect { propertiesItemViewStates ->
            emit(propertiesItemViewStates)
        }
    }

    private fun formatType(type: String): String = when (type) {
        PropertyTypeEntity.DUPLEX.name -> application.resources.getString(PropertyTypeEntity.DUPLEX.stringRes)
        PropertyTypeEntity.FLAT.name -> application.resources.getString(PropertyTypeEntity.FLAT.stringRes)
        PropertyTypeEntity.HOUSE.name -> application.resources.getString(PropertyTypeEntity.HOUSE.stringRes)
        PropertyTypeEntity.LOFT.name -> application.resources.getString(PropertyTypeEntity.LOFT.stringRes)
        PropertyTypeEntity.OTHER.name -> application.resources.getString(PropertyTypeEntity.OTHER.stringRes)
        PropertyTypeEntity.PENTHOUSE.name -> application.resources.getString(PropertyTypeEntity.PENTHOUSE.stringRes)
        else -> ""
    }

    private fun formatFeatures(bedrooms: Int, bathrooms: Int, surface: Double, surfaceUnit: SurfaceUnit, isTablet: Boolean): String {
        val (mappedSurface, mappedSurfaceUnit) = formatSurface(surface, surfaceUnit)

        return if (isTablet) {
            application.resources.getString(
                R.string.property_features_tablet,
                application.resources.getQuantityString(R.plurals.bedroom_plural, bedrooms, bedrooms),
                application.resources.getQuantityString(R.plurals.bathroom_plural, bathrooms, bathrooms),
                application.resources.getString(R.string.property_surface, mappedSurface, mappedSurfaceUnit)
            )
        } else {
            application.resources.getString(R.string.property_features, bedrooms, bathrooms, mappedSurface, mappedSurfaceUnit)
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

    fun onAddPropertyClicked() {
        navigateUseCase.invoke(To.AddProperty)
    }
}
