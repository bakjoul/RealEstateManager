package com.bakjoul.realestatemanager.ui.add

sealed class AddPropertyViewAction {
    object HideSuggestions : AddPropertyViewAction()
    object OpenCamera : AddPropertyViewAction()
    object CloseDialog : AddPropertyViewAction()
    object OpenSettings : AddPropertyViewAction()
}
