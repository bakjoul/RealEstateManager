package com.bakjoul.realestatemanager.data.agent

import android.util.Log
import com.bakjoul.realestatemanager.data.agent.model.AgentResponse
import com.bakjoul.realestatemanager.domain.agent.AgentRepository
import com.bakjoul.realestatemanager.domain.auth.LoggedUserEntity
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class AgentRepositoryImplementation @Inject constructor(private val firestoreDb: FirebaseFirestore): AgentRepository {

    private companion object {
        const val TAG = "AgentRepositoryImplem"
    }

    override fun createFirestoreAgent(currentUser: LoggedUserEntity?) {
        if (currentUser != null) {
            firestoreDb.collection("agents")
                .document(currentUser.id)
                .set(mapUserEntityToDto(currentUser))
        } else {
            Log.d(TAG, "Could not create agent in firestore, current user is null")
        }
    }

    private fun mapUserEntityToDto(currentUser: LoggedUserEntity): AgentResponse {
        return AgentResponse(
            id = currentUser.id,
            displayName = currentUser.displayName,
            email = currentUser.email,
            photoUrl = currentUser.photoUrl
        )
    }
}
