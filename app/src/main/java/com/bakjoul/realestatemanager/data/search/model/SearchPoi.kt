package com.bakjoul.realestatemanager.data.search.model

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.bakjoul.realestatemanager.R

enum class SearchPoi(@StringRes val poiName: Int, @IdRes val poiResId: Int) {
    AIRPORT(R.string.property_poi_airport, R.id.search_transportation_airport_Chip),
    BUS(R.string.property_poi_bus, R.id.search_transportation_bus_Chip),
    HOSPITAL(R.string.property_poi_hospital, R.id.search_amenities_hospital_Chip),
    PARK(R.string.property_poi_park, R.id.search_amenities_park_Chip),
    RESTAURANT(R.string.property_poi_restaurant, R.id.search_amenities_restaurant_Chip),
    SCHOOL(R.string.property_poi_school, R.id.search_amenities_school_Chip),
    STORE(R.string.property_poi_store, R.id.search_amenities_store_Chip),
    SUBWAY(R.string.property_poi_subway, R.id.search_transportation_subway_Chip),
    TRAIN(R.string.property_poi_train, R.id.search_transportation_train_Chip),
    TRAMWAY(R.string.property_poi_tramway, R.id.search_transportation_tramway_Chip),
}
