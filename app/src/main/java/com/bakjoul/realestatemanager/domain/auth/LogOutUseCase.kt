package com.bakjoul.realestatemanager.domain.auth

import com.bakjoul.realestatemanager.domain.navigation.NavigateUseCase
import com.bakjoul.realestatemanager.domain.navigation.model.To
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LogOutUseCase @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val navigateUseCase: NavigateUseCase,
) {
    fun invoke() {
        firebaseAuth.signOut()
        navigateUseCase.invoke(To.Dispatcher)
    }
}
