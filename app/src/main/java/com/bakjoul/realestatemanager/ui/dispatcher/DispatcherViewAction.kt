package com.bakjoul.realestatemanager.ui.dispatcher

sealed class DispatcherViewAction {
    object NavigateToMainScreen : DispatcherViewAction()
    object NavigateToAuthScreen : DispatcherViewAction()
}
