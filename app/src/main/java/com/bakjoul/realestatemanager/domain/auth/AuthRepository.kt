package com.bakjoul.realestatemanager.domain.auth

interface AuthRepository {

    fun getCurrentUser(): LoggedUserEntity?
}
