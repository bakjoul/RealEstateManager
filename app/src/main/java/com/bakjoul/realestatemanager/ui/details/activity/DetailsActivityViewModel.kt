package com.bakjoul.realestatemanager.ui.details.activity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bakjoul.realestatemanager.domain.current_photo.GetCurrentPhotoIdAsEventUseCase
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DetailsActivityViewModel @Inject constructor(
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    isTabletUseCase: IsTabletUseCase,
    getCurrentPhotoIdAsEventUseCase: GetCurrentPhotoIdAsEventUseCase
) : ViewModel() {

    val detailsActivityViewActionLiveData: LiveData<Event<DetailsActivityViewAction>> =
        combine(
            isTabletUseCase.invoke(),
            getCurrentPhotoIdAsEventUseCase.invoke().onEach {
                Log.d("test", "combine photoid onEach: $it")
            }
        ) { isTablet, currentPhotoId ->
            Log.d("test", "combine currentPhotoId: $currentPhotoId")
            if (!isTablet && currentPhotoId != -1) {
                Log.d("test", "emit display photo dialog view action")
                DetailsActivityViewAction.DisplayPhotosDialog
            } else {
                null
            }
        }.filterNotNull().map {
            Event(it)
        }.asLiveData()

    fun onResume(isTablet: Boolean) = refreshOrientationUseCase.invoke(isTablet)
}