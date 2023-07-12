package com.bakjoul.realestatemanager.ui.main

sealed class MainViewAction {
    object NavigateToDetails : MainViewAction()
    object DisplayDetailsFragment : MainViewAction()
    object DisplayEmptyFragment: MainViewAction()
    object DisplayPhotosDialog: MainViewAction()
}
