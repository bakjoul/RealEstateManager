package com.bakjoul.realestatemanager.domain.property.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val price: Double,
    val surface: Double,
    val rooms: Int,
    val bedrooms: Int,
    val bathrooms: Int,
    val description: String,
    val address: String,
    val apartment: String,
    val zipcode: String,
    val city: String,
    val state: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "poi_park") val poiPark: Boolean,
    @ColumnInfo(name = "poi_restaurant") val poiRestaurant: Boolean,
    @ColumnInfo(name = "poi_school") val poiSchool: Boolean,
    @ColumnInfo(name = "poi_store") val poiStore: Boolean,
    @ColumnInfo(name = "poi_hospital") val poiHospital: Boolean,
    @ColumnInfo(name = "poi_airport") val poiAirport: Boolean,
    @ColumnInfo(name = "poi_bus") val poiBus: Boolean,
    @ColumnInfo(name = "poi_subway") val poiSubway: Boolean,
    @ColumnInfo(name = "poi_tramway") val poiTramway: Boolean,
    @ColumnInfo(name = "poi_train") val poiTrain: Boolean,
    @ColumnInfo(name = "on_sale_since") val entryDate: LocalDate,
    @ColumnInfo(name = "date_of_sale") val soldDate: LocalDate?,
    val agent: String,
)
