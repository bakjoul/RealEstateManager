package com.bakjoul.realestatemanager.domain.navigation.model

import com.bakjoul.realestatemanager.ui.utils.NativeText

sealed class To {
    object Details : To()
    object CloseDetails : To()
    object PhotosDialog : To()
    object ClosePhotosDialog : To()
    object DraftDialog : To()
    object DraftListDialog : To()
    object ShowDraftLoadingProgressBar : To()
    object CloseDraftDialog : To()
    object SaveDraftDialog : To()
    data class AddProperty(val draftId: Long, val isNewDraft: Boolean) : To()
    object CloseAddProperty : To()
    object HideAddressSuggestions : To()
    data class Camera(val propertyId: Long) : To()
    object CloseCamera : To()
    data class PhotoPreview(val propertyId: Long) : To()
    object ClosePhotoPreview : To()
    object Dispatcher : To()
    object Settings : To()
    object CloseSettings : To()
    data class Toast(val message: NativeText) : To()
    object LoanSimulatorDialog : To()
    object CloseLoanSimulator : To()
}
