package com.bakjoul.realestatemanager.ui.utils

import java.util.UUID

object IdGenerator {
    fun generateNewIdAsLong(): Long {
        val uuid = UUID.randomUUID()
        return uuid.mostSignificantBits and Long.MAX_VALUE
    }
}
