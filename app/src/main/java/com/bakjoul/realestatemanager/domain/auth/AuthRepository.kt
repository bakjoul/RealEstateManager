package com.bakjoul.realestatemanager.domain.auth

import com.bakjoul.realestatemanager.domain.auth.model.LoggedUserEntity

interface AuthRepository {

    fun getCurrentUser(): LoggedUserEntity?
}
