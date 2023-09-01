package com.bakjoul.realestatemanager.domain.property.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "property_id") val propertyId: Long,
    val url: String,
    val description: String
)
