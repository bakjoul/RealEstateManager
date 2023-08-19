package com.bakjoul.realestatemanager.ui.camera.photo_preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.camera.GetCapturedPhotoUriUseCase
import com.bakjoul.realestatemanager.domain.camera.SetShouldShowPhotoPreviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PhotoPreviewViewModel @Inject constructor(
    private val getCapturedPhotoUriUseCase: GetCapturedPhotoUriUseCase,
    private val setShouldShowPhotoPreviewUseCase: SetShouldShowPhotoPreviewUseCase
) : ViewModel() {

    val viewStateLiveData: LiveData<PhotoPreviewViewState> = liveData {
        getCapturedPhotoUriUseCase.invoke().collect {
            emit(PhotoPreviewViewState(it, null))
        }
    }

    fun onPhotoPreviewDismissed() {
        setShouldShowPhotoPreviewUseCase.invoke(false)
    }
}
