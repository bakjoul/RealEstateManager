package com.bakjoul.realestatemanager.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.data.search.model.SearchParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(

) : ViewModel() {

    private val searchParamsMutableStateFlow: MutableStateFlow<SearchParams> = MutableStateFlow(SearchParams())

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
}
