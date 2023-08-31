package com.bakjoul.realestatemanager.data.property

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {

    @Insert
    suspend fun insert(propertyEntity: PropertyEntity): Long

    @Query("SELECT * FROM properties")
    fun getProperties(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE id = :propertyId")
    fun getPropertyById(propertyId: Long): Flow<PropertyEntity>
}