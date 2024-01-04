package com.bakjoul.realestatemanager.ui.drafts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
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
import com.bakjoul.realestatemanager.ui.utils.NativeText
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
                        navigateUseCase.invoke(To.AddProperty(propertyDraft.id, false))
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
    ) : NativeText {
        val price = if (propertyDraft.referencePrice != null) {
            NativeText.Simple(formatPrice(propertyDraft.referencePrice, currency, euroRate))
        } else {
            NativeText.Simple("Price N/A")
        }

        val surface = if (propertyDraft.referenceSurface != null) {
            NativeText.Arguments(
                R.string.draft_overview_field,
                listOf(
                    formatSurface(propertyDraft.referenceSurface, surfaceUnit),
                    NativeText.Resource(surfaceUnit.unitSymbol),
                )
            )
        } else {
            NativeText.Simple("Surf. N/A")
        }

        val rooms = if (propertyDraft.rooms != null) {
            NativeText.Arguments(
                R.string.draft_overview_field,
                listOf(
                    propertyDraft.rooms,
                    NativeText.Resource(R.string.add_property_label_rooms),
                )
            )
        } else {
            NativeText.Simple("Rooms N/A")
        }

        val bedrooms = if (propertyDraft.bedrooms != null) {
            NativeText.Arguments(
                R.string.draft_overview_field,
                listOf(
                    propertyDraft.bedrooms,
                    NativeText.Resource(R.string.draft_overview_label_bedrooms),
                )
            )
        } else {
            NativeText.Simple("Bed. N/A")
        }

        val bathrooms = if (propertyDraft.bathrooms != null) {
            NativeText.Arguments(
                R.string.draft_overview_field,
                listOf(
                    propertyDraft.bathrooms,
                    NativeText.Resource(R.string.draft_overview_label_bathrooms),
                )
            )
        } else {
            NativeText.Simple("Bath. N/A")
        }

        return NativeText.Multi(
            listOf(
                price,
                surface,
                rooms,
                bedrooms,
                bathrooms,
            ),
            separator = " - "
        )
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
