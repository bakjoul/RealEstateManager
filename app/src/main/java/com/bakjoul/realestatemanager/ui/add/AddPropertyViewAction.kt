package com.bakjoul.realestatemanager.ui.add

import com.bakjoul.realestatemanager.ui.utils.NativeText

sealed class AddPropertyViewAction {
    object HideSuggestions : AddPropertyViewAction()
    data class OpenCamera(val propertyId: Long, val isExistingProperty: Boolean) : AddPropertyViewAction()
    data class ShowImportedPhotoPreview(val propertyId: Long, val isExistingProperty: Boolean) : AddPropertyViewAction()
    data class EditPhotoDescription(val photoId: Long, val description: String, val isExistingProperty: Boolean) : AddPropertyViewAction()
    data class ShowPhotosViewer(val propertyId: Long, val clickedPhotoIndex: Int, val isExistingProperty: Boolean) : AddPropertyViewAction()
    object SaveDraftDialog : AddPropertyViewAction()
    object CloseDialog : AddPropertyViewAction()
    object OpenAppSettings : AddPropertyViewAction()
    data class ShowToast(val message: NativeText) : AddPropertyViewAction()
}
