package com.bakjoul.realestatemanager.data.photos.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_photos")
data class PendingPhotoDtoEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "property_id") val propertyId: Long,
    val url: String,
    val description: String
)
