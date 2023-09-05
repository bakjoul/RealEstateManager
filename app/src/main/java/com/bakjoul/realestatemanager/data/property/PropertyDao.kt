package com.bakjoul.realestatemanager.data.property

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.bakjoul.realestatemanager.data.property.model.PropertyDtoEntity
import com.bakjoul.realestatemanager.data.property.model.PropertyWithPhotosEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {

    @Insert
    suspend fun insert(property: PropertyDtoEntity): Long

    @Query("SELECT * FROM properties")
    @Transaction
    fun getProperties(): Flow<List<PropertyWithPhotosEntity>>

    @Query("SELECT * FROM properties WHERE id = :propertyId")
    @Transaction
    fun getPropertyById(propertyId: Long): PropertyWithPhotosEntity?
}
