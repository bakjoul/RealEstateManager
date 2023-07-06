package com.bakjoul.realestatemanager.domain.auth

data class LoggedUserEntity(
    val id: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?
)
