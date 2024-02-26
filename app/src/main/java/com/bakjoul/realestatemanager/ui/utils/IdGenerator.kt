package com.bakjoul.realestatemanager.ui.utils

import java.util.UUID

object IdGenerator {
    fun generateNewIdAsLong(): Long {
        val uuid = UUID.randomUUID()
        return uuid.mostSignificantBits and Long.MAX_VALUE
    }

    fun generateShortUuid(): String {
        val uuid = UUID.randomUUID()
        return uuid.toString().substring(0, 8)
    }
}
