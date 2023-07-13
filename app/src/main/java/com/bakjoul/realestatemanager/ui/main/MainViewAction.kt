package com.bakjoul.realestatemanager.ui.main

sealed class MainViewAction {
    object NavigateToDetails : MainViewAction()
    object DisplayPhotosDialog: MainViewAction()
}
