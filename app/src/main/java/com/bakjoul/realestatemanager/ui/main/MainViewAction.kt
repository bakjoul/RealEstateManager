package com.bakjoul.realestatemanager.ui.main

sealed class MainViewAction {
    object ShowDetailsTablet : MainViewAction()
    object CloseDetailsTablet : MainViewAction()
    object ShowDetailsPortrait : MainViewAction()
    object CloseDetailsPortrait : MainViewAction()
    object ShowDetailsPortraitIfNeeded : MainViewAction()
    object HideDetailsPortrait : MainViewAction()
    object ShowPhotosDialog : MainViewAction()
    object ShowPhotosDialogAndHideDetailsPortrait : MainViewAction()
    object ShowPropertyDraftDialog: MainViewAction()
    data class ShowAddPropertyDialog(val propertyId: Long?, val propertyDraftId: Long?) : MainViewAction()
    object ReturnToDispatcher : MainViewAction()
    object ShowSettings : MainViewAction()
    object ShowSettingsAndHideDetailsPortrait : MainViewAction()
}
