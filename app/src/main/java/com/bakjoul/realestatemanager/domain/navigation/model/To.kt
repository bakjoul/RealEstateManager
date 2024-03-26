package com.bakjoul.realestatemanager.domain.navigation.model

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.ui.utils.NativeText

sealed class To {
    object Details : To()
    object CloseDetails : To()
    data class Photos(val propertyId: Long, val clickedPhotoIndex: Int) : To()
    object ClosePhotos : To()
    object AddPropertyDraftAlertDialog : To()
    object DraftList : To()
    object ShowDraftLoadingProgressBar : To()
    object CloseDraftList : To()
    object CloseDraftListInBackground : To()
    object SaveDraftDialog : To()
    data class AddProperty(val draftId: Long, val isNewDraft: Boolean) : To()
    object CloseAddProperty : To()
    object HideAddressSuggestions : To()
    data class Camera(val propertyId: Long, val isExistingProperty: Boolean) : To()
    object CloseCamera : To()
    data class ImportedPhotoPreview(val propertyId: Long, val isExistingProperty: Boolean) : To()
    data class PhotoPreview(val propertyId: Long, val isExistingProperty: Boolean) : To()
    object ClosePhotoPreview : To()
    data class EditPhotoDescription(val photoId: Long, val description: String, val isExistingProperty: Boolean) : To()
    object CloseEditPhotoDescription : To()
    data class DraftPhotos(val clickedPhotoIndex: Int, val isExistingProperty: Boolean) : To()
    object CloseDraftPhotos : To()
    data class EditPropertyDraftAlertDialog(val property: PropertyEntity) : To()
    data class EditProperty(val propertyId: Long): To()
    object Dispatcher : To()
    object Settings : To()
    object CloseSettings : To()
    object AppSettings : To()
    data class Toast(val message: NativeText) : To()
    object LoanSimulator : To()
    object CloseLoanSimulator : To()
    object DoNothing : To()
}
