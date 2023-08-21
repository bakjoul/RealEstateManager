package com.bakjoul.realestatemanager.ui.details.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bakjoul.realestatemanager.domain.current_photo.GetPhotosDialogViewActionUseCase
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.photos.PhotosDialogViewAction
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DetailsActivityViewModel @Inject constructor(
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    isTabletUseCase: IsTabletUseCase,
    getPhotosDialogViewActionUseCase: GetPhotosDialogViewActionUseCase
) : ViewModel() {

    val detailsActivityViewActionLiveData: LiveData<Event<DetailsActivityViewAction>> =
        combine(
            isTabletUseCase.invoke(),
            getPhotosDialogViewActionUseCase.invoke()
        ) { isTablet, dialogViewAction ->
            if (!isTablet && dialogViewAction is PhotosDialogViewAction.ShowPhotosDialog) {
                DetailsActivityViewAction.ShowPhotosDialog
            } else {
                null
            }
        }.filterNotNull().map {
            Event(it)
        }.asLiveData()

    fun onResume(isTablet: Boolean) = refreshOrientationUseCase.invoke(isTablet)
}
