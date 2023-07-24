package com.bakjoul.realestatemanager.domain.auth.model

data class LoggedUserEntity(
    val id: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?
)
