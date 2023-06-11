package com.bakjoul.realestatemanager.data.property

import com.bakjoul.realestatemanager.domain.property.PropertyRepository
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyRepositoryImplementation @Inject constructor() : PropertyRepository {
    private val propertiesMutableStateFlow: MutableStateFlow<List<PropertyEntity>> =
        MutableStateFlow(
            listOf(
                PropertyEntity(
                    id = 0,
                    type = PropertyType.Flat.name,
                    price = 100000,
                    surface = 100,
                    rooms = 5,
                    bedrooms = 3,
                    bathrooms = 2,
                    description = "Anchored by a vast marble gallery with sweeping staircase, the entertaining floor includes a baronial living room facing Park Avenue, handsome library with original paneling, and tremendous dining room; all of which enjoy fireplaces. The state-of-the-art St. Charles designed kitchen includes a sunny breakfast room and staff quarters. Upstairs, the expansive master suite overlooks Park Avenue and includes two marble baths, two dressing rooms, and two offices. Additionally there are three large bedrooms with en-suite baths and a media room.",
                    address = "740 Park Avenue",
                    apartment = "6/7A",
                    zipcode = "NY 10021",
                    city = "New York",
                    state = "New York",
                    country = "United States",
                    latitude = 48.856614,
                    longitude = 2.3522219,
                    photos = listOf(
                        PhotoEntity(
                            0,
                            0,
                            "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                            "Lounge"
                        ),
                        PhotoEntity(
                            1,
                            0,
                            "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                            "Lounge 2"
                        )
                    ),
                    poiPark = true,
                    poiRestaurant = true,
                    poiSchool = true,
                    poiStore = true,
                    poiHospital = false,
                    poiAirport = false,
                    poiBus = true,
                    poiSubway = false,
                    poiTramway = false,
                    poiTrain = true,
                    isSold = false,
                    entryDate = LocalDate.parse("2023-01-01"),
                    soldDate = null,
                    agent = "John Doe"
                ),
                PropertyEntity(
                    id = 1,
                    type = PropertyType.House.name,
                    price = 200000,
                    surface = 200,
                    rooms = 10,
                    bedrooms = 5,
                    bathrooms = 3,
                    description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget ultricies ultrices, nisl nisl aliquam",
                    address = "2 rue de la paix",
                    apartment = "2",
                    zipcode = "75000",
                    city = "Paris",
                    state = "Ile de France",
                    country = "France",
                    latitude = 48.856614,
                    longitude = 2.3522219,
                    photos = listOf(
                        PhotoEntity(
                            0,
                            0,
                            "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                            "Lounge"
                        )
                    ),
                    poiPark = true,
                    poiRestaurant = true,
                    poiSchool = true,
                    poiStore = true,
                    poiHospital = true,
                    poiAirport = true,
                    poiBus = true,
                    poiSubway = true,
                    poiTramway = true,
                    poiTrain = true,
                    isSold = false,
                    entryDate = LocalDate.parse("2023-02-01"),
                    soldDate = null,
                    agent = "Jane Doe"
                )
            )
        )

    override fun getPropertiesFlow(): Flow<List<PropertyEntity>> =
        propertiesMutableStateFlow.asStateFlow()

    override suspend fun getPropertyById(id: Long): PropertyEntity? = withContext(Dispatchers.IO) {
        propertiesMutableStateFlow.value.find { it.id == id }
    }
}
