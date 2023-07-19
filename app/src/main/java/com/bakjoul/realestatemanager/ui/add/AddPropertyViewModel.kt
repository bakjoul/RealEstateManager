package com.bakjoul.realestatemanager.ui.add

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.property.PropertyType
import com.bakjoul.realestatemanager.data.settings.model.AppCurrency
import com.bakjoul.realestatemanager.data.settings.model.SurfaceUnit
import com.bakjoul.realestatemanager.domain.settings.currency.GetCurrentCurrencyUseCase
import com.bakjoul.realestatemanager.domain.settings.surface_unit.GetCurrentSurfaceUnitUseCase
import com.bakjoul.realestatemanager.ui.utils.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class AddPropertyViewModel @Inject constructor(
    private val getCurrentCurrencyUseCase: GetCurrentCurrencyUseCase,
    private val getCurrentSurfaceUnitUseCase: GetCurrentSurfaceUnitUseCase,
    private val application: Application
) : ViewModel() {

    private val propertyTypeMutableStateFlow: MutableStateFlow<PropertyType?> = MutableStateFlow(null)
    private val isForSaleMutableStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val numberOfRoomsMutableStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private val numberOfBathroomsMutableStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private val numberOfBedroomsMutableStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)

    val viewStateLiveData: LiveData<AddPropertyViewState> = liveData {
        combine(
            getCurrentCurrencyUseCase.invoke(),
            getCurrentSurfaceUnitUseCase.invoke(),
            propertyTypeMutableStateFlow,
            isForSaleMutableStateFlow,
            numberOfRoomsMutableStateFlow,
            numberOfBathroomsMutableStateFlow,
            numberOfBedroomsMutableStateFlow
        ) { currency, surfaceUnit, propertyType, isForSale, numberOfRooms, numberOfBathrooms, numberOfBedrooms ->
            AddPropertyViewState(
                propertyType = propertyType,
                dateHint = formatDateHint(isForSale),
                priceHint = formatPriceHint(currency),
                surfaceHint = formatSurfaceHint(surfaceUnit),
                numberOfRooms = numberOfRooms.toString(),
                numberOfBathrooms = numberOfBathrooms.toString(),
                numberOfBedrooms = numberOfBedrooms.toString()
            )
        }.collect {
            emit(it)
        }
    }

    private fun formatPriceHint(currency: AppCurrency): String {
        return "Price (${currency.symbol})"
    }

    private fun formatDateHint(isForSale: Boolean): String {
        return if (isForSale) {
            application.getString(R.string.property_for_sale_since)
        } else {
            application.getString(R.string.property_sold_on)
        }
    }

    private fun formatSurfaceHint(surfaceUnit: SurfaceUnit): String {
        return "Surface (${surfaceUnit.unit})"
    }

    fun onPropertyTypeChanged(checkedId: Int) {
        propertyTypeMutableStateFlow.value = when (checkedId) {
            R.id.add_property_type_flat_RadioButton -> PropertyType.Flat
            R.id.add_property_type_house_RadioButton -> PropertyType.House
            R.id.add_property_type_duplex_RadioButton -> PropertyType.Duplex
            R.id.add_property_type_penthouse_RadioButton -> PropertyType.Penthouse
            R.id.add_property_type_loft_RadioButton -> PropertyType.Loft
            R.id.add_property_type_other_RadioButton -> PropertyType.Other
            else -> null
        }
    }

    fun onSaleStatusChanged(isForSale: Boolean) {
        isForSaleMutableStateFlow.value = isForSale
    }

    fun decrementRooms() {
        val currentValue = numberOfRoomsMutableStateFlow.value
        if (currentValue > 0) {
            numberOfRoomsMutableStateFlow.value = currentValue - 1
        }
    }

    fun incrementRooms() {
        numberOfRoomsMutableStateFlow.value = numberOfRoomsMutableStateFlow.value.plus(1)
    }

    fun decrementBathrooms() {
        val currentValue = numberOfBathroomsMutableStateFlow.value
        if (currentValue > 0) {
            numberOfBathroomsMutableStateFlow.value = currentValue - 1
        }
    }

    fun incrementBathrooms() {
        numberOfBathroomsMutableStateFlow.value = numberOfBathroomsMutableStateFlow.value.plus(1)
    }

    fun decrementBedrooms() {
        val currentValue = numberOfBedroomsMutableStateFlow.value
        if (currentValue > 0) {
            numberOfBedroomsMutableStateFlow.value = currentValue - 1
        }
    }

    fun incrementBedrooms() {
        numberOfBedroomsMutableStateFlow.value = numberOfBedroomsMutableStateFlow.value.plus(1)
    }
}
