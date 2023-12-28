package com.bakjoul.realestatemanager.domain.navigation

import com.bakjoul.realestatemanager.domain.navigation.model.To
import javax.inject.Inject

class NavigateUseCase @Inject constructor(private val navigationRepository: NavigationRepository) {
    fun invoke(to: To) {
        navigationRepository.setCurrentDestination(to)
    }
}
