package com.bakjoul.realestatemanager.ui.main

sealed class MainViewAction {
    object ShowDetails : MainViewAction()
    object ShowPhotosDialog: MainViewAction()
    object ShowAddPropertyActivity: MainViewAction()
    object ShowAddPropertyDialog: MainViewAction()
    object DoNothing: MainViewAction()
}
