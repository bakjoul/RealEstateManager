package com.bakjoul.realestatemanager.ui.details.activity

sealed class DetailsActivityViewAction {
    object ShowPhotosDialog: DetailsActivityViewAction()
    object CloseActivity: DetailsActivityViewAction()
}