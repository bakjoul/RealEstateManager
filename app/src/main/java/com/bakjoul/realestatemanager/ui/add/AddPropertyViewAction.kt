package com.bakjoul.realestatemanager.ui.add

sealed class AddPropertyViewAction {
    object HideSuggestions : AddPropertyViewAction()
    data class OpenCamera(val propertyId: Long) : AddPropertyViewAction()
    object SaveDraftDialog : AddPropertyViewAction()
    object CloseDialog : AddPropertyViewAction()
    object OpenSettings : AddPropertyViewAction()
    data class ShowToast(val message: String) : AddPropertyViewAction()
}
