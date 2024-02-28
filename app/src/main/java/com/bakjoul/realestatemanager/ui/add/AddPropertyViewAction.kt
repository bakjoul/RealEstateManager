package com.bakjoul.realestatemanager.ui.add

import com.bakjoul.realestatemanager.ui.utils.NativeText

sealed class AddPropertyViewAction {
    object HideSuggestions : AddPropertyViewAction()
    data class OpenCamera(val propertyId: Long) : AddPropertyViewAction()
    data class ShowImportedPhotoPreview(val propertyId: Long) : AddPropertyViewAction()
    data class EditPhotoDescription(val photoId: Long, val description: String) : AddPropertyViewAction()
    object SaveDraftDialog : AddPropertyViewAction()
    object CloseDialog : AddPropertyViewAction()
    object OpenSettings : AddPropertyViewAction()
    data class ShowToast(val message: NativeText) : AddPropertyViewAction()
}
