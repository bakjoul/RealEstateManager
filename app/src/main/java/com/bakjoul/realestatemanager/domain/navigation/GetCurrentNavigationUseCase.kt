package com.bakjoul.realestatemanager.domain.navigation

import com.bakjoul.realestatemanager.domain.navigation.model.To
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentNavigationUseCase @Inject constructor(
    private val navigationRepository: NavigationRepository,
) {
    fun invoke(): Flow<To> = navigationRepository.getCurrentDestination()
}