package com.bakjoul.realestatemanager.data.auth

import com.bakjoul.realestatemanager.domain.auth.AuthRepository
import com.bakjoul.realestatemanager.domain.auth.model.LoggedUserEntity
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImplementation @Inject constructor(private val firebaseAuth: FirebaseAuth): AuthRepository {

    override fun getCurrentUser(): LoggedUserEntity? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let { user ->
            val displayName = user.displayName
            val email = user.email

            if (displayName != null && email != null) {
                LoggedUserEntity(
                    id = user.uid,
                    displayName = displayName,
                    email = email,
                    photoUrl = user.photoUrl.toString()
                )
            } else {
                null
            }
        }
    }
}
