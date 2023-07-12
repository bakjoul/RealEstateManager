package com.bakjoul.realestatemanager.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bakjoul.realestatemanager.domain.agent.AgentRepository
import com.bakjoul.realestatemanager.domain.auth.GetCurrentUserUseCase
import com.bakjoul.realestatemanager.domain.auth.IsUserAuthenticatedUseCase
import com.bakjoul.realestatemanager.domain.auth.LogOutUseCase
import com.bakjoul.realestatemanager.domain.current_photo.GetCurrentPhotoIdUseCase
import com.bakjoul.realestatemanager.domain.current_property.GetCurrentPropertyIdChannelUseCase
import com.bakjoul.realestatemanager.domain.resources.IsTabletUseCase
import com.bakjoul.realestatemanager.domain.resources.RefreshOrientationUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getCurrentPropertyIdChannelUseCase: GetCurrentPropertyIdChannelUseCase,
    isTabletUseCase: IsTabletUseCase,
    getCurrentPhotoIdUseCase: GetCurrentPhotoIdUseCase,
    isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase,
    agentRepository: AgentRepository,
    private val refreshOrientationUseCase: RefreshOrientationUseCase,
    private val logOutUseCase: LogOutUseCase
) : ViewModel() {

    init {
        viewModelScope.launch {
            isUserAuthenticatedUseCase.invoke().collect { isAuthenticated ->
                if (isAuthenticated) {
                    val currentUser = getCurrentUserUseCase.invoke()
                    agentRepository.createFirestoreAgent(currentUser)
                }
            }
        }
    }

    val mainViewActionLiveData: LiveData<Event<MainViewAction>> =
        combine(
            getCurrentPropertyIdChannelUseCase.invoke().receiveAsFlow(),
            isTabletUseCase.invoke(),
            getCurrentPhotoIdUseCase.invoke()
        ) { id, isTablet, currentPhotoId ->
            Log.d("test", "combine id: $id, isTablet: $isTablet, currentPhotoId: $currentPhotoId")
            if (currentPhotoId != -1) {
                MainViewAction.DisplayPhotosDialog
            } else if (id >= 0) {
                if (isTablet) {
                    MainViewAction.DisplayDetailsFragment
                } else {
                        Log.d("test", "navigatetodetails: ")
                        MainViewAction.NavigateToDetails
                }
            } else {
                MainViewAction.DisplayEmptyFragment
            }
        }.filterNotNull().map {
            Event(it)
        }.asLiveData()

    fun onResume(isTablet: Boolean) {
        refreshOrientationUseCase.invoke(isTablet)
    }

    fun logOut() = logOutUseCase.invoke()
}
