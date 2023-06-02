package com.bakjoul.realestatemanager.domain.property.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "real_estate")
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "price") val price: Int,
    @ColumnInfo(name = "surface") val surface: Int,
    @ColumnInfo(name = "rooms") val rooms: Int,
    @ColumnInfo(name = "bedrooms") val bedrooms: Int,
    @ColumnInfo(name = "bathrooms") val bathrooms: Int,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "appartment") val apartment: String,
    @ColumnInfo(name = "zipcode") val zipcode: Int,
    @ColumnInfo(name = "city") val city: String,
    @ColumnInfo(name = "state") val state: String,
    @ColumnInfo(name = "country") val country: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "poi_school") val poiSchool: Boolean,
    @ColumnInfo(name = "poi_store") val poiStore: Boolean,
    @ColumnInfo(name = "poi_park") val poiPark: Boolean,
    @ColumnInfo(name = "poi_restaurant") val poiRestaurant: Boolean,
    @ColumnInfo(name = "poi_train") val poiTrain: Boolean,
    @ColumnInfo(name = "poi_bus") val poiBus: Boolean,
    @ColumnInfo(name = "poi_airport") val poiAirport: Boolean,
    @ColumnInfo(name = "is_sold") val isSold: Boolean,
    @ColumnInfo(name = "on_sale_since") val entryDate: LocalDate,
    @ColumnInfo(name = "date_of_sale") val soldDate: LocalDate?,
    @ColumnInfo(name = "agent") val agent: String,
)
