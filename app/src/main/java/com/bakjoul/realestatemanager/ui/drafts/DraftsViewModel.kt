package com.bakjoul.realestatemanager.ui.drafts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.bakjoul.realestatemanager.domain.property.drafts.GetPropertyDraftsFlowUseCase
import com.bakjoul.realestatemanager.ui.utils.EquatableCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DraftsViewModel @Inject constructor(
    private val getPropertyDraftsFlowUseCase: GetPropertyDraftsFlowUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    val draftsLiveData: LiveData<List<DraftsItemViewState>> = liveData {
        getPropertyDraftsFlowUseCase.invoke().map {
            it.map { propertyDraft ->
                DraftsItemViewState(
                    id = propertyDraft.id,
                    entryDate = propertyDraft.entryDate.toString(),
                    onDraftItemClicked = EquatableCallback {
                        navigateUseCase.invoke(To.AddProperty)
                    }
                )
            }
        }.collect {
            emit(it)
        }
    }

}
