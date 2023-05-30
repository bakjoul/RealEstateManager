package com.bakjoul.realestatemanager.data

import android.content.Context
import com.bakjoul.realestatemanager.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourcesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val isTabletMutableStateFlow = MutableStateFlow<Boolean?>(null)

    fun isTabletFlow(): Flow<Boolean> = isTabletMutableStateFlow.filterNotNull()

    fun refreshOrientation() {
        isTabletMutableStateFlow.value = context.resources.getBoolean(R.bool.isTablet)
    }
}
