package com.bakjoul.realestatemanager.ui.main

sealed class MainViewAction {
    object ShowPortraitDetails : MainViewAction()
    object ShowTabletDetails : MainViewAction()
    object ClearDetailsTablet : MainViewAction()
    object ShowPhotosDialog : MainViewAction()
    object ShowAddPropertyDialog : MainViewAction()
}
