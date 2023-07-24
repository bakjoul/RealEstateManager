package com.bakjoul.realestatemanager.domain.auth

import com.bakjoul.realestatemanager.domain.auth.model.LoggedUserEntity
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(private val authRepository: AuthRepository) {
    fun invoke(): LoggedUserEntity? = authRepository.getCurrentUser()
}
