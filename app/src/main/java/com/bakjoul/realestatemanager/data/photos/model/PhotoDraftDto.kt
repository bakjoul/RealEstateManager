package com.bakjoul.realestatemanager.data.photos.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_drafts")
data class PhotoDraftDto (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "property_form_id") val propertyFormId: Long,
    val url: String,
    val description: String
)
