package com.bakjoul.realestatemanager.domain.resources

import javax.inject.Inject

class RefreshOrientationUseCase @Inject constructor(private val resourcesRepository: ResourcesRepository) {
    fun invoke(isTablet: Boolean) {
        resourcesRepository.setOrientation(isTablet)
    }
}
