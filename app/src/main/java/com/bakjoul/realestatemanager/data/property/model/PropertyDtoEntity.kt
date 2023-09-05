package com.bakjoul.realestatemanager.data.property.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "properties")
data class PropertyDtoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    @ColumnInfo("on_sale_since") val entryDate: LocalDate,
    @ColumnInfo("date_of_sale") val saleDate: LocalDate?,
    val price: BigDecimal,
    val surface: Double,
    val rooms: Int,
    val bathrooms: Int,
    val bedrooms: Int,
    @ColumnInfo(name = "poi_airport") val poiAirport: Boolean,
    @ColumnInfo(name = "poi_bus") val poiBus: Boolean,
    @ColumnInfo(name = "poi_hospital") val poiHospital: Boolean,
    @ColumnInfo(name = "poi_park") val poiPark: Boolean,
    @ColumnInfo(name = "poi_restaurant") val poiRestaurant: Boolean,
    @ColumnInfo(name = "poi_school") val poiSchool: Boolean,
    @ColumnInfo(name = "poi_store") val poiStore: Boolean,
    @ColumnInfo(name = "poi_subway") val poiSubway: Boolean,
    @ColumnInfo(name = "poi_train") val poiTrain: Boolean,
    @ColumnInfo(name = "poi_tramway") val poiTramway: Boolean,
    val address: String,
    val apartment: String,
    val zipcode: String,
    val city: String,
    val state: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val agent: String,
)
