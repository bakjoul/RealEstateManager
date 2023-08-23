package com.bakjoul.realestatemanager.domain.current_property

import com.bakjoul.realestatemanager.ui.main.MainViewAction
import kotlinx.coroutines.flow.Flow

interface CurrentPropertyIdRepository {
    fun setCurrentPropertyId(currentId: Long)

    fun resetCurrentPropertyId()

    fun getCurrentPropertyIdFlow(): Flow<Long>

    fun setDetailsViewAction(viewAction: MainViewAction)

    fun getDetailsViewActionFlow(): Flow<MainViewAction>
}
