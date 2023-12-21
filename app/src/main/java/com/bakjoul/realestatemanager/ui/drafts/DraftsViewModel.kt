package com.bakjoul.realestatemanager.ui.drafts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.CoroutineDispatcherProvider
import com.bakjoul.realestatemanager.domain.currency_rate.GetEuroRateUseCase
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.property.drafts.GetPropertyDraftsFlowUseCase
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import com.bakjoul.realestatemanager.ui.utils.Event
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatPrice
import com.bakjoul.realestatemanager.ui.utils.ViewModelUtils.Companion.formatSurface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DraftsViewModel @Inject constructor(
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getEuroRateUseCase: GetEuroRateUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    private val getPropertyDraftsFlowUseCase: GetPropertyDraftsFlowUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    val draftsLiveData: LiveData<List<DraftsItemViewState>> = liveData(coroutineDispatcherProvider.io) {
        combine(
            getPropertyDraftsFlowUseCase.invoke(),
            getCurrentCurrencyUseCase.invoke(),
            flow { emit(getEuroRateUseCase.invoke()) },
            getCurrentSurfaceUnitUseCase.invoke()
        ) { draft, currency, euroRateWrapper, surfaceUnit ->
            draft.map { propertyDraft ->
                DraftsItemViewState(
                    id = propertyDraft.id,
                    photoUrl = propertyDraft.photos!!.firstOrNull()?.url ?: "",
                    lastUpdate = formatDate(propertyDraft.lastUpdate),
                    typeAndLocation = formatTypeAndLocation(propertyDraft),
                    overview = formatOverview(propertyDraft, currency, euroRateWrapper.currencyRateEntity.rate, surfaceUnit),
                    description = formatDescription(propertyDraft.description),
                    onDraftItemClicked = EquatableCallback {
                        navigateUseCase.invoke(To.AddProperty(propertyDraft.id, null))
                    }
                )
            }
        }.collect {
            emit(it)
        }
    }

    val viewActionLiveData: LiveData<Event<DraftsViewAction>> = liveData {
        getCurrentNavigationUseCase.invoke().collect {
            when (it) {
                is To.AddProperty -> {
                    // TODO REFACTO CLOSE WHEN ADD DIALOG OPENED INSTEAD
                    viewModelScope.launch {
                        emit(Event(DraftsViewAction.ShowProgressBar))
                        delay(2000)
                        emit(Event(DraftsViewAction.CloseDialog))
                    }
                }
                is To.CloseDraftDialog -> emit(Event(DraftsViewAction.CloseDialog))
                else -> Unit
            }
        }
    }

    private fun formatTypeAndLocation(propertyDraft: PropertyFormEntity) = buildString {
        if (propertyDraft.type != null) {
            append(propertyDraft.type.name)
        } else {
            append("Type N/A")
        }
        append(" - ")
        if (propertyDraft.address?.city != null) {
            append(propertyDraft.address.city)
        } else {
            append("City N/A")
        }
    }

    private fun formatOverview(
        propertyDraft: PropertyFormEntity,
        currency: AppCurrency,
        euroRate: Double,
        surfaceUnit: SurfaceUnit
    ) = buildString {
        var formattedSurface: Pair<Int, String>? = null
        if (propertyDraft.referenceSurface != null) {
            formattedSurface = formatSurface(propertyDraft.referenceSurface, surfaceUnit)
        }

        if (propertyDraft.referencePrice != null) {
            append(formatPrice(propertyDraft.referencePrice, currency, euroRate))
        } else {
            append("Price N/A")
        }
        append(" - ")
        if (formattedSurface != null) {
            append("${formattedSurface.first} ${formattedSurface.second}")
        } else {
            append("Surf. N/A")
        }
        append(" - ")
        if (propertyDraft.rooms != null) {
            append("${propertyDraft.rooms} rooms")
        } else {
            append("Rooms N/A")
        }
        append(" - ")
        if (propertyDraft.bedrooms != null) {
            append("${propertyDraft.bedrooms} bedrooms")
        } else {
            append("Bed. N/A")
        }
        append(" - ")
        if (propertyDraft.bathrooms != null) {
            append("${propertyDraft.bathrooms} bathrooms")
        } else {
            append("Bath. N/A")
        }
    }

    private fun formatDescription(description: String?): String {
        return if (!description.isNullOrEmpty()) {
            description
        } else {
            "Description N/A"
        }
    }

    private fun formatDate(lastUpdate: LocalDateTime): String = "Last edited on ${lastUpdate.format(dateFormatter)}"

    fun closeDialog() {
        navigateUseCase.invoke(To.CloseDraftDialog)
    }
}
