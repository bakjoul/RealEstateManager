package com.bakjoul.realestatemanager.data.resources

import android.content.Context
import com.bakjoul.realestatemanager.R
import com.bakjoul.realestatemanager.domain.resources.ResourcesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourcesRepositoryImplementation @Inject constructor(@ApplicationContext private val context: Context) :
    ResourcesRepository {

    private val isTabletMutableStateFlow = MutableStateFlow<Boolean?>(null)

    override fun isTabletFlow(): Flow<Boolean> = isTabletMutableStateFlow.filterNotNull()

    override fun refreshOrientation() {
        isTabletMutableStateFlow.value = context.resources.getBoolean(R.bool.isTablet)
    }
}
