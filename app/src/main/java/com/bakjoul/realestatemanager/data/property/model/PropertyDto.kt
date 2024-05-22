package com.bakjoul.realestatemanager.data.property.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "properties")
data class PropertyDto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    @ColumnInfo("for_sale_since") val forSaleSince: LocalDate,
    @ColumnInfo("date_of_sale") val dateOfSale: LocalDate?,
    val price: BigDecimal,
    val surface: BigDecimal,
    val rooms: BigDecimal,
    val bathrooms: BigDecimal,
    val bedrooms: BigDecimal,
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
    @ColumnInfo(name = "street_number") val streetNumber: String,
    val route: String,
    @ColumnInfo(name = "complementary_address") val complementaryAddress: String?,
    val zipcode: String,
    val city: String,
    @ColumnInfo(name = "administrative_area_level_1") val administrativeAreaLevel1: String,
    @ColumnInfo(name = "administrative_area_level_2") val administrativeAreaLevel2: String?,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val featuredPhotoId: Long?,
    val agent: String,
    @ColumnInfo(name = "entry_date") val entryDate: LocalDateTime
)
