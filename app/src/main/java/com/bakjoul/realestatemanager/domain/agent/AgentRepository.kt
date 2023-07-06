package com.bakjoul.realestatemanager.domain.agent

import com.bakjoul.realestatemanager.domain.auth.LoggedUserEntity

interface AgentRepository {

    fun createFirestoreAgent(currentUser: LoggedUserEntity?)
}
