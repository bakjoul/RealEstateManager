package com.bakjoul.realestatemanager.ui.main

sealed class MainViewAction {
    object ShowDetailsTablet : MainViewAction()
    object CloseDetailsTablet : MainViewAction()
    object ShowDetailsPortrait : MainViewAction()
    object ShowDetailsPortraitAndPhotosDialog : MainViewAction()
    object ShowDetailsTabletAndPhotosDialog : MainViewAction()
    object ClosePhotosDialogAndOpenDetailsPortrait : MainViewAction()
    object ClosePhotosDialog : MainViewAction()
    object ShowAddPropertyDialog : MainViewAction()
    object ReturnToDispatcher : MainViewAction()
    object ShowSettings : MainViewAction()
}
