package com.bakjoul.realestatemanager.data.property

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bakjoul.realestatemanager.data.property.model.PropertyFormDto
import com.bakjoul.realestatemanager.data.property.model.PropertyFormWithPhotosDto
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyFormDao {

    @Insert
    suspend fun insert(propertyForm: PropertyFormDto): Long?

    @Query("SELECT COUNT(*) FROM property_drafts WHERE id = :propertyFormId")
    suspend fun getPropertyFormIdCount(propertyFormId: Long): Int

    @Update
    suspend fun update(propertyForm: PropertyFormDto): Int

    @Query("SELECT EXISTS(SELECT id FROM property_drafts)")
    suspend fun hasPropertyForms(): Boolean

    @Query("SELECT * FROM property_drafts")
    @Transaction
    fun getPropertyForms(): Flow<List<PropertyFormWithPhotosDto>>

    @Query("SELECT * FROM property_drafts WHERE id = :propertyFormId")
    @Transaction
    suspend fun getPropertyFormById(propertyFormId: Long): PropertyFormWithPhotosDto?

    @Query("DELETE FROM property_drafts WHERE id = :id")
    suspend fun delete(id: Long): Int
}
