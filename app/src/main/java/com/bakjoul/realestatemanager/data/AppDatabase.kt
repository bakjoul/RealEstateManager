package com.bakjoul.realestatemanager.data

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bakjoul.realestatemanager.data.photos.PhotoDao
import com.bakjoul.realestatemanager.data.photos.edit.TemporaryPhotoDao
import com.bakjoul.realestatemanager.data.photos.model.PhotoDto
import com.bakjoul.realestatemanager.data.photos.model.TemporaryPhotoDto
import com.bakjoul.realestatemanager.data.property.PropertyDao
import com.bakjoul.realestatemanager.data.property.PropertyFormDao
import com.bakjoul.realestatemanager.data.property.model.PropertyDto
import com.bakjoul.realestatemanager.data.property.model.PropertyFormDto
import com.bakjoul.realestatemanager.data.utils.type_converters.BigDecimalTypeConverter
import com.bakjoul.realestatemanager.data.utils.type_converters.LocalDateTimeTypeConverter
import com.bakjoul.realestatemanager.data.utils.type_converters.LocalDateTypeConverter
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.google.gson.Gson
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Database(
    entities = [
        PropertyDto::class,
        PhotoDto::class,
        PropertyFormDto::class,
        TemporaryPhotoDto::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    LocalDateTypeConverter::class,
    LocalDateTimeTypeConverter::class,
    BigDecimalTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getPropertyDao(): PropertyDao
    abstract fun getPropertyFormDao(): PropertyFormDao
    abstract fun getPhotoDao(): PhotoDao
    abstract fun getTemporaryPhotoDao(): TemporaryPhotoDao

    companion object {
        private const val DATABASE_NAME = "RealEstateManager_database"

        fun create(
            application: Application,
            workManager: WorkManager,
            gson: Gson,
        ): AppDatabase {
            val builder = Room.databaseBuilder(
                application,
                AppDatabase::class.java,
                DATABASE_NAME
            )

            builder.addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    val propertiesAsJson = gson.toJson(
                        listOf(
                            PropertyDto(
                                id = 1,
                                type = PropertyTypeEntity.FLAT.name,
                                forSaleSince = LocalDate.parse("2023-01-01"),
                                dateOfSale = LocalDate.parse("2023-01-02"),
                                price = BigDecimal(100000),
                                surface = BigDecimal(100),
                                rooms = BigDecimal(5),
                                bathrooms = BigDecimal(2),
                                bedrooms = BigDecimal(3),
                                poiAirport = false,
                                poiBus = true,
                                poiHospital = false,
                                poiPark = true,
                                poiRestaurant = true,
                                poiSchool = true,
                                poiStore = true,
                                poiSubway = false,
                                poiTrain = true,
                                poiTramway = false,
                                streetNumber = "740",
                                route = "Park Avenue",
                                complementaryAddress = "6/7A",
                                zipcode = "NY 10021",
                                city = "New York",
                                administrativeAreaLevel1 = "New York",
                                administrativeAreaLevel2 = null,
                                country = "United States",
                                latitude = 48.856614,
                                longitude = 2.3522219,
                                description = "Anchored by a vast marble gallery with sweeping staircase, the entertaining floor includes a baronial living room facing Park Avenue, handsome library with original paneling, and tremendous dining room; all of which enjoy fireplaces. The state-of-the-art St. Charles designed kitchen includes a sunny breakfast room and staff quarters. Upstairs, the expansive master suite overlooks Park Avenue and includes two marble baths, two dressing rooms, and two offices. Additionally there are three large bedrooms with en-suite baths and a media room.",
                                featuredPhotoId = 1,
                                agent = "John Doe",
                                entryDate = LocalDateTime.parse("2023-01-01T00:00:00")
                            ),
                            PropertyDto(
                                id = 2,
                                type = PropertyTypeEntity.HOUSE.name,
                                forSaleSince = LocalDate.parse("2023-02-01"),
                                dateOfSale = null,
                                price = BigDecimal(200000),
                                surface = BigDecimal(200),
                                rooms = BigDecimal(10),
                                bathrooms = BigDecimal(4),
                                bedrooms = BigDecimal(4),
                                poiAirport = true,
                                poiBus = true,
                                poiHospital = true,
                                poiPark = true,
                                poiRestaurant = true,
                                poiSchool = true,
                                poiStore = true,
                                poiSubway = false,
                                poiTrain = true,
                                poiTramway = true,
                                streetNumber = "2",
                                route = "Rue de la paix",
                                complementaryAddress = "A2",
                                zipcode = "75000",
                                city = "Paris",
                                administrativeAreaLevel1 = "Ile de France",
                                administrativeAreaLevel2 = null,
                                country = "France",
                                latitude = 48.856614,
                                longitude = 2.3522219,
                                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget ultricies ultrices, nisl nisl aliquam",
                                featuredPhotoId = 6,
                                agent = "Jane Doe",
                                entryDate = LocalDateTime.parse("2023-02-01T00:00:00")
                            ),
                            PropertyDto(
                                id = 3,
                                type = PropertyTypeEntity.DUPLEX.name,
                                forSaleSince = LocalDate.parse("2023-04-01"),
                                dateOfSale = null,
                                price = BigDecimal(300000),
                                surface = BigDecimal(300),
                                rooms = BigDecimal(12),
                                bathrooms = BigDecimal(5),
                                bedrooms = BigDecimal(5),
                                poiAirport = false,
                                poiBus = true,
                                poiHospital = true,
                                poiPark = true,
                                poiRestaurant = true,
                                poiSchool = true,
                                poiStore = true,
                                poiSubway = false,
                                poiTrain = true,
                                poiTramway = true,
                                streetNumber = "22",
                                route = "Rue de la paix",
                                complementaryAddress = "B22",
                                zipcode = "75000",
                                city = "Paris",
                                administrativeAreaLevel1 = "Ile de France",
                                administrativeAreaLevel2 = null,
                                country = "France",
                                latitude = 48.856614,
                                longitude = 2.3522219,
                                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget ultricies ultrices, nisl nisl aliquam",
                                featuredPhotoId = 7,
                                agent = "Jane Doe",
                                entryDate = LocalDateTime.parse("2023-04-01T00:00:00")
                            ),
                        )
                    )

                    val photosAsJson = gson.toJson(
                        listOf(
                            PhotoDto(
                                id = 1,
                                propertyId = 1,
                                uri = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge 1"
                            ),
                            PhotoDto(
                                id = 2,
                                propertyId = 1,
                                uri = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge 2"
                            ),
                            PhotoDto(
                                id = 3,
                                propertyId = 1,
                                uri = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge 3"
                            ),
                            PhotoDto(
                                id = 4,
                                propertyId = 1,
                                uri = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge 4"
                            ),
                            PhotoDto(
                                id = 5,
                                propertyId = 1,
                                uri = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge 5"
                            ),
                            PhotoDto(
                                id = 6,
                                propertyId = 2,
                                uri = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge"
                            ),
                            PhotoDto(
                                id = 7,
                                propertyId = 3,
                                uri = "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                description = "Lounge"
                            ),
                        )
                    )

                    workManager.enqueue(
                        OneTimeWorkRequestBuilder<InitializeDatabaseWorker>()
                            .setInputData(
                                workDataOf(
                                    InitializeDatabaseWorker.KEY_INPUT_DATA_PROPERTIES to propertiesAsJson,
                                    InitializeDatabaseWorker.KEY_INPUT_DATA_PHOTOS to photosAsJson
                                )
                            )
                            .build()
                    )
                }
            })
            return builder.build()
        }
    }
}
