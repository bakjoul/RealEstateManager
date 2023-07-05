package com.bakjoul.realestatemanager.ui.dispatcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bakjoul.realestatemanager.domain.auth.IsUserAuthenticatedUseCase
import com.bakjoul.realestatemanager.ui.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DispatcherViewModel @Inject constructor(isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase) : ViewModel() {

    val dispatcherViewActionLiveData: LiveData<Event<DispatcherViewAction>> =
        isUserAuthenticatedUseCase.invoke().map { isAuthenticated ->
            val action = if (isAuthenticated) {
                DispatcherViewAction.NavigateToMainScreen
            } else {
                DispatcherViewAction.NavigateToAuthScreen
            }
            Event(action)
        }.asLiveData()
}
