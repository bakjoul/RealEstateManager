package com.bakjoul.realestatemanager.ui.details.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bakjoul.realestatemanager.domain.navigation.GetCurrentNavigationUseCase
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DetailsActivityViewModel @Inject constructor(
    isTabletUseCase: IsTabletUseCase,
    getCurrentNavigationUseCase: GetCurrentNavigationUseCase,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    val detailsActivityViewActionLiveData: LiveData<Event<DetailsActivityViewAction>> =
        combine(
            isTabletUseCase.invoke(),
            getCurrentNavigationUseCase.invoke()
        ) { isTablet, navigation ->
            if (!isTablet) {
                when (navigation) {
                    is To.PhotosDialog -> DetailsActivityViewAction.ShowPhotosDialog
                    is To.CloseDetails -> DetailsActivityViewAction.CloseActivity
                    else -> null
                }
            } else {
                null
            }
        }.filterNotNull().map {
            Event(it)
        }.asLiveData()

    fun onResume(isTablet: Boolean) {
        refreshOrientationUseCase.invoke(isTablet)
    }

    fun onConfigurationChanged() {
        navigateUseCase.invoke(To.CloseDetails)
    }
}
