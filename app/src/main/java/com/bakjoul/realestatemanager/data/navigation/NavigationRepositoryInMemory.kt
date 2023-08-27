package com.bakjoul.realestatemanager.data.navigation

import com.bakjoul.realestatemanager.domain.navigation.NavigationRepository
import com.bakjoul.realestatemanager.domain.navigation.model.To
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationRepositoryInMemory @Inject constructor(): NavigationRepository {
    private val destinationMutableSharedFlow: MutableSharedFlow<To> = MutableSharedFlow(replay = 1)

    override fun setCurrentDestination(to: To) {
        destinationMutableSharedFlow.tryEmit(to)
    }

    override fun getCurrentDestination(): Flow<To> = destinationMutableSharedFlow.asSharedFlow()
}
