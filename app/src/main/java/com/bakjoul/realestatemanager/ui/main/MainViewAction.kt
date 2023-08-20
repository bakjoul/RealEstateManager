package com.bakjoul.realestatemanager.ui.main

sealed class MainViewAction {
    object ShowDetails : MainViewAction()
    object ShowPhotosDialog: MainViewAction()
    object ShowAddPropertyFragment: MainViewAction()
    object CloseAddPropertyFragment: MainViewAction()
    object ShowAddPropertyActivity: MainViewAction()
}
