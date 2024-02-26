package com.bakjoul.realestatemanager.ui.camera

import androidx.lifecycle.ViewModel
import com.bakjoul.realestatemanager.domain.photo_preview.SetLastPhotoUriUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val setLastPhotoUriUseCase: SetLastPhotoUriUseCase,
    private val navigateUseCase: NavigateUseCase,
) : ViewModel() {

    fun onImageSaved(uri: String?, propertyId: Long) = uri?.let {
        setLastPhotoUriUseCase.invoke(it)
        navigateUseCase.invoke(To.PhotoPreview(propertyId))
    }

    fun onCloseButtonClicked() {
        navigateUseCase.invoke(To.CloseCamera)
    }
}
