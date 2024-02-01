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
    object ShowPropertyDraftDialog : MainViewAction()
    object ShowDraftListDialog : MainViewAction()
    data class ShowAddPropertyDialog(val draftId: Long, val isNewDraft: Boolean) : MainViewAction()
    object ReturnToDispatcher : MainViewAction()
    object ShowSettings : MainViewAction()
    object ShowSettingsAndHideDetailsPortrait : MainViewAction()
    object ShowLoanSimulatorDialog : MainViewAction()
}
