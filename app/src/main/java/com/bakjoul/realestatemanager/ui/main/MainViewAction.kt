package com.bakjoul.realestatemanager.ui.main

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
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
    data class ShowPhotosDialogAndDetailsPortraitIfNeeded(val propertyId: Long, val clickedPhotoIndex: Int) : MainViewAction()
    data class ShowPhotosAndHideDetailsPortrait(val propertyId: Long, val clickedPhotoIndex: Int) : MainViewAction()
    object ShowPropertyDraftAlertDialog : MainViewAction()
    object ShowDraftListAndDetailsPortraitIfNeeded : MainViewAction()
    object ShowDraftListAndHideDetailsPortraitIfNeeded : MainViewAction()
    data class ShowAddPropertyAndDetailsPortraitIfNeeded(val draftId: Long, val isNewDraft: Boolean) : MainViewAction()
    data class ShowAddPropertyAndHideDetailsPortraitIfNeeded(val draftId: Long, val isNewDraft: Boolean) : MainViewAction()
    data class ShowEditPropertyDraftAlertDialog(val property: PropertyEntity) : MainViewAction()
    data class ShowEditPropertyAndDetailsPortraitIfNeeded(val propertyId: Long): MainViewAction()
    data class ShowEditPropertyAndHideDetailsPortraitIfNeeded(val propertyId: Long): MainViewAction()
    data class ShowDeletePropertyAlertDialog(val property: PropertyEntity) : MainViewAction()
    object ReturnToDispatcher : MainViewAction()
    object ShowSettingsAndDetailsPortraitIfNeeded : MainViewAction()
    object ShowSettingsAndHideDetailsPortraitIfNeeded : MainViewAction()
    object ShowLoanSimulatorAndDetailsPortraitIfNeeded : MainViewAction()
    object ShowLoanSimulatorAndHideDetailsPortraitIfNeeded : MainViewAction()
    data class ShowEditPropertyErrorToastAndDetailsTabletIfNeeded(val message: NativeText, val showToast: Boolean): MainViewAction()
    data class ShowEditPropertyErrorToastAndDetailsPortraitIfNeeded(val message: NativeText, val showToast: Boolean): MainViewAction()
    object ShowFilterDialogAndHideDetailsPortraitIfNeeded: MainViewAction()
    object ShowFilterDialogAndDetailsPortraitIfNeeded: MainViewAction()
}
