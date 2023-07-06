package com.bakjoul.realestatemanager.domain.auth

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LogOutUseCase @Inject constructor(private val firebaseAuth: FirebaseAuth) {
    fun invoke() = firebaseAuth.signOut()
}
