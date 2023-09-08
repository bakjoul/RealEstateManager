package com.bakjoul.realestatemanager.ui.main

sealed class MainViewAction {
    //object ShowDetailsTablet : MainViewAction()
    //object CloseDetailsTablet : MainViewAction()
    object ShowDetailsPortrait : MainViewAction()
    object ShowPhotosDialog : MainViewAction()
    object ShowAddPropertyDialog : MainViewAction()
    object ReturnToDispatcher : MainViewAction()
    object ShowSettings : MainViewAction()
}
