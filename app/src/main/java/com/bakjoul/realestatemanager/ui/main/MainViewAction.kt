package com.bakjoul.realestatemanager.ui.main

sealed class MainViewAction {
    object ShowDetailsTablet : MainViewAction()
    object CloseDetailsTablet : MainViewAction()
    object ShowDetailsPortrait : MainViewAction()
    object CloseDetailsPortrait : MainViewAction()
    object ShowDetailsPortraitIfNeeded : MainViewAction()
    object HideDetailsPortrait : MainViewAction()
    object ShowPhotosDialog : MainViewAction()
    object ShowAddPropertyDialog : MainViewAction()
    object ReturnToDispatcher : MainViewAction()
    object ShowSettings : MainViewAction()
    object ShowSettingsAndHideDetailsPortrait : MainViewAction()
}
