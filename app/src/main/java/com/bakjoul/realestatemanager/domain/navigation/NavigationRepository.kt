package com.bakjoul.realestatemanager.domain.navigation

import com.bakjoul.realestatemanager.domain.navigation.model.To
import kotlinx.coroutines.flow.Flow

interface NavigationRepository {

    fun setCurrentDestination(to: To)

    fun getCurrentDestination(): Flow<To>
}