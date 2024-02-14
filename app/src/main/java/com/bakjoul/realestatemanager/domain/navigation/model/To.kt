package com.bakjoul.realestatemanager.domain.navigation.model

import com.bakjoul.realestatemanager.ui.utils.NativeText

sealed class To {
    object Details : To()
    object CloseDetails : To()
    object Photos : To()
    object ClosePhotos : To()
    object DraftAlertDialog : To()
    object DraftList : To()
    object ShowDraftLoadingProgressBar : To()
    object CloseDraftList : To()
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
    object LoanSimulator : To()
    object CloseLoanSimulator : To()
}
