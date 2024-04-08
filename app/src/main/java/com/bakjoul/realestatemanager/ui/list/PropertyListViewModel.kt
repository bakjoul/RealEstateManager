package com.bakjoul.realestatemanager.ui.list

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.currency_rate.GetEuroRateUseCase
import com.bakjoul.realestatemanager.domain.current_property.SetCurrentPropertyIdUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.property.GetPropertiesFlowUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.AddPropertyDraftUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.GenerateNewDraftIdUseCase
import com.bakjoul.realestatemanager.domain.property.drafts.GetPropertyDraftIdsUseCase
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.NativeText
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatPrice
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatSurfaceValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PropertyListViewModel @Inject constructor(
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val getPropertiesFlowUseCase: GetPropertiesFlowUseCase,
    private val setCurrentPropertyIdUseCase: SetCurrentPropertyIdUseCase,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getEuroRateUseCase: GetEuroRateUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val isTabletUseCase: IsTabletUseCase,
    private val navigateUseCase: NavigateUseCase,
    private val getPropertyDraftIdsUseCase: GetPropertyDraftIdsUseCase,
    private val generateNewDraftIdUseCase: GenerateNewDraftIdUseCase,
    private val addPropertyDraftUseCase: AddPropertyDraftUseCase,
    private val clock: Clock
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var existingProperties = emptyList<PropertyEntity>()

    val propertiesLiveData: LiveData<List<PropertyItemViewState>> = liveData(coroutineDispatcherProvider.io) {
        combine(
            getPropertiesFlowUseCase.invoke(),
            getCurrentCurrencyUseCase.invoke(),
            flow { emit(getEuroRateUseCase.invoke()) },
            getCurrentSurfaceUnitUseCase.invoke(),
            isTabletUseCase.invoke(),
        ) { properties, currency, euroRateWrapper, surfaceUnit, isTablet ->
            if (properties != existingProperties) {
                existingProperties = properties
            }

            properties.map { property ->
                PropertyItemViewState(
                    id = property.id,
                    photoUrl = property.photos.find { it.id == property.featuredPhotoId }?.uri ?: "",
                    type = formatType(property.type),
                    city = property.address.city,
                    features = formatFeatures(property.bedrooms, property.bathrooms, property.surface, surfaceUnit, isTablet),
                    price = formatPrice(property.price, currency, euroRateWrapper.currencyRateEntity.rate),
                    currencyRate = formatRate(
                        currency,
                        euroRateWrapper.currencyRateEntity.rate,
                        euroRateWrapper.currencyRateEntity.updateDate.format(dateFormatter)
                    ),
                    isSold = property.saleDate != null,
                    onPropertyClicked = EquatableCallback {
                        setCurrentPropertyIdUseCase.invoke(property.id)
                        navigateUseCase.invoke(To.Details)
                    }
                )
            }
        }.collect { propertiesItemViewStates ->
            emit(propertiesItemViewStates)
        }
    }

    private fun formatType(type: String): NativeText = when (type) {
        PropertyTypeEntity.DUPLEX.name -> NativeText.Resource(PropertyTypeEntity.DUPLEX.typeName)
        PropertyTypeEntity.FLAT.name -> NativeText.Resource(PropertyTypeEntity.FLAT.typeName)
        PropertyTypeEntity.HOUSE.name -> NativeText.Resource(PropertyTypeEntity.HOUSE.typeName)
        PropertyTypeEntity.LOFT.name -> NativeText.Resource(PropertyTypeEntity.LOFT.typeName)
        PropertyTypeEntity.OTHER.name -> NativeText.Resource(PropertyTypeEntity.OTHER.typeName)
        PropertyTypeEntity.PENTHOUSE.name -> NativeText.Resource(PropertyTypeEntity.PENTHOUSE.typeName)
        else -> NativeText.Simple("")
    }

    private fun formatFeatures(
        bedrooms: BigDecimal,
        bathrooms: BigDecimal,
        surface: BigDecimal,
        surfaceUnit: SurfaceUnit,
        isTablet: Boolean
    ): NativeText {
        val parsedSurfaceValue = formatSurfaceValue(surface, surfaceUnit)

        return if (isTablet) {
            NativeText.Arguments(
                R.string.property_features_tablet,
                listOf(
                    NativeText.Plural(R.plurals.bedroom_plural, bedrooms.toInt(), listOf(bedrooms.toInt())),
                    NativeText.Plural(R.plurals.bathroom_plural, bathrooms.toInt(), listOf(bathrooms.toInt())),
                    NativeText.Arguments(
                        R.string.property_surface,
                        listOf(
                            parsedSurfaceValue,
                            NativeText.Resource(surfaceUnit.unitSymbol),
                        )
                    )
                )
            )
        } else {
            NativeText.Arguments(
                R.string.property_features,
                listOf(
                    bedrooms.toInt(),
                    bathrooms.toInt(),
                    NativeText.Arguments(
                        R.string.property_surface,
                        listOf(
                            parsedSurfaceValue,
                            NativeText.Resource(surfaceUnit.unitSymbol),
                        )
                    )
                )
            )
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
        viewModelScope.launch {
            val propertyDraftIds = getPropertyDraftIdsUseCase.invoke()
            if (propertyDraftIds.isNotEmpty()) {
                val existingPropertyIds = existingProperties.map { it.id }
                val newPropertyDraftIds = propertyDraftIds.filter { !existingPropertyIds.contains(it) }
                if (newPropertyDraftIds.isNotEmpty()) {
                    navigateUseCase.invoke(To.AddPropertyDraftAlertDialog)
                    return@launch
                }
            }
            val propertyDraftId = generateNewDraftIdUseCase.invoke()
            addPropertyDraftUseCase.invoke(
                PropertyFormEntity(
                    propertyDraftId,
                    lastUpdate = ZonedDateTime.now(clock).toLocalDateTime()
                )
            )
            navigateUseCase.invoke(To.AddProperty(propertyDraftId, true))
        }
    }
}
