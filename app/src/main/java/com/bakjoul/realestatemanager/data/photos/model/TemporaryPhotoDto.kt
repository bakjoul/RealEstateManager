package com.bakjoul.realestatemanager.data.photos.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "temporary_photos")
data class TemporaryPhotoDto (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "property_id") val propertyId: Long,
    val uri: String,
    val description: String
)
