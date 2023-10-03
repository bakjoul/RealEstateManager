package com.bakjoul.realestatemanager.data.property.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "property_drafts")
data class PropertyFormDto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String?,
    @ColumnInfo(name = "is_sold") val isSold: Boolean?,
    @ColumnInfo("for_sale_since") val forSaleSince: LocalDate?,
    @ColumnInfo("date_of_sale") val dateOfSale: LocalDate?,
    val price: BigDecimal?,
    val surface: BigDecimal?,
    val rooms: Int?,
    val bathrooms: Int?,
    val bedrooms: Int?,
    @ColumnInfo(name = "poi_airport") val poiAirport: Boolean?,
    @ColumnInfo(name = "poi_bus") val poiBus: Boolean?,
    @ColumnInfo(name = "poi_hospital") val poiHospital: Boolean?,
    @ColumnInfo(name = "poi_park") val poiPark: Boolean?,
    @ColumnInfo(name = "poi_restaurant") val poiRestaurant: Boolean?,
    @ColumnInfo(name = "poi_school") val poiSchool: Boolean?,
    @ColumnInfo(name = "poi_store") val poiStore: Boolean?,
    @ColumnInfo(name = "poi_subway") val poiSubway: Boolean?,
    @ColumnInfo(name = "poi_train") val poiTrain: Boolean?,
    @ColumnInfo(name = "poi_tramway") val poiTramway: Boolean?,
    @ColumnInfo(name = "ac_street_number") val autoCompleteStreetNumber: String?,
    @ColumnInfo(name = "ac_route") val autoCompleteRoute: String?,
    @ColumnInfo(name = "ac_complementary_address") val autoCompleteComplementaryAddress: String?,
    @ColumnInfo(name = "ac_zipcode") val autoCompleteZipcode: String?,
    @ColumnInfo(name = "ac_city") val autoCompleteCity: String?,
    @ColumnInfo(name = "ac_state") val autoCompleteState: String?,
    @ColumnInfo(name = "ac_country") val autoCompleteCountry: String?,
    @ColumnInfo(name = "ac_latitude") val autoCompleteLatitude: Double?,
    @ColumnInfo(name = "ac_longitude") val autoCompleteLongitude: Double?,
    @ColumnInfo(name = "street_number") val streetNumber: String?,
    val route: String?,
    @ColumnInfo(name = "complementary_address") val complementaryAddress: String?,
    val zipcode: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val latitude: Double?,
    val longitude: Double?,
    val description: String?,
    val agent: String?,
)
