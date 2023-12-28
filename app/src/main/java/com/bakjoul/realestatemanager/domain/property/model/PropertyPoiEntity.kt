package com.bakjoul.realestatemanager.domain.property.model

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.bakjoul.realestatemanager.R

enum class PropertyPoiEntity(@StringRes val poiName: Int, @IdRes val poiResId: Int) {
    AIRPORT(R.string.property_poi_airport, R.id.add_property_transportation_airport_Chip),
    BUS(R.string.property_poi_bus, R.id.add_property_transportation_bus_Chip),
    HOSPITAL(R.string.property_poi_hospital, R.id.add_property_amenities_hospital_Chip),
    PARK(R.string.property_poi_park, R.id.add_property_amenities_park_Chip),
    RESTAURANT(R.string.property_poi_restaurant, R.id.add_property_amenities_restaurant_Chip),
    SCHOOL(R.string.property_poi_school,R.id.add_property_amenities_school_Chip),
    STORE(R.string.property_poi_store, R.id.add_property_amenities_store_Chip),
    SUBWAY(R.string.property_poi_subway, R.id.add_property_transportation_subway_Chip),
    TRAIN(R.string.property_poi_train, R.id.add_property_transportation_train_Chip),
    TRAMWAY(R.string.property_poi_tramway, R.id.add_property_transportation_tramway_Chip),
}
