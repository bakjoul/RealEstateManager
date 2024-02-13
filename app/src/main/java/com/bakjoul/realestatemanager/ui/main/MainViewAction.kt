package com.bakjoul.realestatemanager.ui.main

import com.bakjoul.realestatemanager.ui.utils.NativeText

sealed class MainViewAction {
    object ShowDetailsTablet : MainViewAction()
    object CloseDetailsTablet : MainViewAction()
    object ShowDetailsPortrait : MainViewAction()
    object CloseDetailsPortrait : MainViewAction()
    object ShowDetailsPortraitIfNeeded : MainViewAction()
    object HideDetailsPortrait : MainViewAction()
    data class ShowClipboardToastAndDetailsTabletIfNeeded(val message: NativeText, val showToast: Boolean) : MainViewAction()
    data class ShowClipboardToastAndDetailsPortraitIfNeeded(val message: NativeText, val showToast: Boolean) : MainViewAction()
    object ShowPhotosDialog : MainViewAction()
    object ShowPhotosDialogAndHideDetailsPortrait : MainViewAction()
    object ShowPropertyDraftDialog : MainViewAction()
    object ShowDraftListDialog : MainViewAction()
    data class ShowAddPropertyDialog(val draftId: Long, val isNewDraft: Boolean) : MainViewAction()
    object ReturnToDispatcher : MainViewAction()
    object ShowSettings : MainViewAction()
    object ShowSettingsAndHideDetailsPortrait : MainViewAction()
    object ShowLoanSimulatorDialog : MainViewAction()
    object ShowLoanSimulatorDialogAndHideDetailsPortrait: MainViewAction()
}
