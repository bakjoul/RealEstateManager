package com.bakjoul.realestatemanager.data.property

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.bakjoul.realestatemanager.data.property.model.PropertyFormDto
import com.bakjoul.realestatemanager.data.property.model.PropertyFormWithPhotosDto
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyFormDao {

    @Insert
    suspend fun insert(propertyForm: PropertyFormDto): Long?

    @Query("SELECT EXISTS(SELECT id FROM property_drafts)")
    suspend fun hasPropertyForms(): Boolean

    @Query("SELECT * FROM property_drafts")
    @Transaction
    fun getPropertyForms(): Flow<List<PropertyFormWithPhotosDto>>

    @Query("SELECT * FROM property_drafts WHERE id = :propertyFormId")
    @Transaction
    fun getPropertyFormById(propertyFormId: Long): Flow<PropertyFormWithPhotosDto?>

    @Query("DELETE FROM property_drafts WHERE id = :id")
    suspend fun delete(id: Long)
}
