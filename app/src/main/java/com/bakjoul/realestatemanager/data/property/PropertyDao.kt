package com.bakjoul.realestatemanager.data.property

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.bakjoul.realestatemanager.data.property.model.PropertyDto
import com.bakjoul.realestatemanager.data.property.model.PropertyWithPhotosDto
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {

    @Insert
    suspend fun insert(property: PropertyDto): Long

    @Query("SELECT * FROM properties")
    @Transaction
    fun getProperties(): Flow<List<PropertyWithPhotosDto>>

    @Query("SELECT * FROM properties WHERE id = :propertyId")
    @Transaction
    fun getPropertyById(propertyId: Long): Flow<PropertyWithPhotosDto?>
}
