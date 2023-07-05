package com.bakjoul.realestatemanager.domain.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class IsUserAuthenticatedUseCase @Inject constructor(private val firebaseAuth: FirebaseAuth) {
    fun invoke(): Flow<Boolean> = flow {
        emit(firebaseAuth.currentUser != null)
    }
}
