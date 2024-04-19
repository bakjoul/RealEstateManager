package com.bakjoul.realestatemanager.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.search.model.SearchDurationUnit
import com.bakjoul.realestatemanager.data.search.model.SearchParams
import com.bakjoul.realestatemanager.data.search.model.SearchPoi
import com.bakjoul.realestatemanager.data.search.model.SearchType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SearchViewModel @Inject constructor(

) : ViewModel() {

    private companion object {
        private val RANGE_UPDATE_DELAY = 300.milliseconds
    }

    private val searchParamsMutableStateFlow: MutableStateFlow<SearchParams> = MutableStateFlow(SearchParams())
    private var priceRangeUpdateJob: Job? = null
    private var surfaceRangeUpdateJob: Job? = null

    fun onStatusChanged(buttonId: Int) {
        Log.d("test", "onStatusChanged: $buttonId")
        searchParamsMutableStateFlow.update {
            it.copy(isSold = when (buttonId) {
                R.id.search_status_for_sale_Button -> false
                R.id.search_status_sold_Button -> true
                else -> null
            })
        }
    }

    fun onDurationChanged(duration: Int?) {
        Log.d("test", "onDurationChanged: $duration")
        searchParamsMutableStateFlow.update {
            it.copy(durationFromEntryOrSaleDate = duration)
        }
    }

    fun onDurationUnitChanged(searchDurationUnit: SearchDurationUnit) {
        Log.d("test", "onDurationUnitChanged: $searchDurationUnit")
        searchParamsMutableStateFlow.update {
            it.copy(
                durationFromEntryOrSaleDateUnit = when (searchDurationUnit) {
                    SearchDurationUnit.WEEKS -> SearchDurationUnit.WEEKS
                    SearchDurationUnit.MONTHS -> SearchDurationUnit.MONTHS
                    SearchDurationUnit.YEARS -> SearchDurationUnit.YEARS
                }
            )
        }
    }

    fun onTypeChipCheckedChanged(chipId: Int, isChecked: Boolean) {
        Log.d("test", "onTypeChipCheckedChanged: $chipId, $isChecked")
        val type = SearchType.values().find { it.chipId == chipId } ?: return
        val currentList = searchParamsMutableStateFlow.value.types ?: emptyList()
        if (isChecked) {
            searchParamsMutableStateFlow.update {
                it.copy(types = currentList.plus(type))
            }
        } else {
            searchParamsMutableStateFlow.update {
                it.copy(types = currentList.minus(type))
            }
        }
        Log.d("test", "onTypeChipCheckedChanged: ${searchParamsMutableStateFlow.value.types}")
    }

    fun onPriceRangeChanged(priceRange: Pair<BigDecimal, BigDecimal>) {
        priceRangeUpdateJob?.cancel()
        priceRangeUpdateJob = viewModelScope.launch {
            delay(RANGE_UPDATE_DELAY)
            searchParamsMutableStateFlow.update {
                it.copy(minPrice = priceRange.first, maxPrice = priceRange.second)
            }
            Log.d("test", "onPriceRangeChanged: ${searchParamsMutableStateFlow.value.minPrice}, ${searchParamsMutableStateFlow.value.maxPrice}")
        }
    }

    fun onSurfaceRangeChanged(surfaceRange: Pair<BigDecimal, BigDecimal>) {
        surfaceRangeUpdateJob?.cancel()
        surfaceRangeUpdateJob = viewModelScope.launch {
            delay(RANGE_UPDATE_DELAY)
            searchParamsMutableStateFlow.update {
                it.copy(minSurface = surfaceRange.first, maxSurface = surfaceRange.second)
            }
            Log.d("test", "onSurfaceRangeChanged: ${searchParamsMutableStateFlow.value.minSurface}, ${searchParamsMutableStateFlow.value.maxSurface}")
        }
    }

    fun onPoiChipCheckedChanged(chipId: Int, isChecked: Boolean) {
        Log.d("test", "onPoiChipCheckedChanged: $chipId, $isChecked")
        val poi = SearchPoi.values().find { it.poiResId == chipId } ?: return
        val currentList = searchParamsMutableStateFlow.value.pointsOfInterest ?: emptyList()
        if (isChecked) {
            searchParamsMutableStateFlow.update {
                it.copy(pointsOfInterest = currentList.plus(poi))
            }
        } else {
            searchParamsMutableStateFlow.update {
                it.copy(pointsOfInterest = currentList.minus(poi))
            }
        }
        Log.d("test", "onPoiChipCheckedChanged: ${searchParamsMutableStateFlow.value.pointsOfInterest}")
    }
}
