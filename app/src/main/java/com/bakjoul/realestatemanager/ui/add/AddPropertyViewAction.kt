package com.bakjoul.realestatemanager.ui.add

sealed class AddPropertyViewAction {
    object HideSuggestions : AddPropertyViewAction()
    object CloseActivity : AddPropertyViewAction()
    object CloseDialog : AddPropertyViewAction()
}
