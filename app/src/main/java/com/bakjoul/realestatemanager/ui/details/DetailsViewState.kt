package com.bakjoul.realestatemanager.ui.details

import com.bakjoul.realestatemanager.designsystem.molecule.photo_list.PhotoListItemViewState

data class DetailsViewState(
    val mainPhotoUrl: String,
    val type: String,
    val price: String,
    val isSold: Boolean,
    val city: String,
    val saleStatus: String,
    val description: String,
    val surface: String,
    val rooms: String,
    val bedrooms: String,
    val bathrooms: String,
    val poiSchool: Boolean,
    val poiStore: Boolean,
    val poiPark: Boolean,
    val poiRestaurant: Boolean,
    val poiHospital: Boolean,
    val poiBus: Boolean,
    val poiSubway: Boolean,
    val poiTramway: Boolean,
    val poiTrain: Boolean,
    val poiAirport: Boolean,
    val location: String,
    val medias: List<PhotoListItemViewState>,
    val clipboardAddress: String,
    val staticMapUrl: String,
    val mapsAddress: String
)
