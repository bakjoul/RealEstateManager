package com.bakjoul.realestatemanager.ui.camera.photo_preview

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.camera.DeleteCapturedPhotoUseCase
import com.bakjoul.realestatemanager.domain.camera.GetCapturedPhotoUriUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PhotoPreviewViewModel @Inject constructor(
    private val getCapturedPhotoUriUseCase: GetCapturedPhotoUriUseCase,
    private val deleteCapturedPhotoUseCase: DeleteCapturedPhotoUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    val viewStateLiveData: LiveData<PhotoPreviewViewState> = liveData {
        getCapturedPhotoUriUseCase.invoke().collect {
            emit(PhotoPreviewViewState(it, null))
        }
    }

    fun onCancelButtonClicked(photoUri: String) {
        deleteCapturedPhotoUseCase.invoke(photoUri.toUri())
        navigateUseCase.invoke(To.ClosePhotoPreview)
    }

    fun onDoneButtonClicked() {
        navigateUseCase.invoke(To.CloseCamera)
    }
}
