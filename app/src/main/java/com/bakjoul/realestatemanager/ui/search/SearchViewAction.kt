package com.bakjoul.realestatemanager.ui.search

import com.bakjoul.realestatemanager.ui.utils.NativeText

sealed class SearchViewAction {
    object HideSuggestions : SearchViewAction()
    data class ShowToast(val message: NativeText) : SearchViewAction()
}
