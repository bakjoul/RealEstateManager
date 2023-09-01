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
import com.bakjoul.realestatemanager.data.property.PropertyDao
import com.bakjoul.realestatemanager.data.property.PropertyType
import com.bakjoul.realestatemanager.data.utils.LocalDateTypeConverter
import com.bakjoul.realestatemanager.domain.property.model.PhotoEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.google.gson.Gson
import java.time.LocalDate

@Database(
    entities = [PropertyEntity::class, PhotoEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getPropertyDao(): PropertyDao

    companion object {
        private const val DATABASE_NAME = "RealEstateManager_database"

        fun create(
            application: Application,
            workManager: WorkManager,
            gson: Gson
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
                            PropertyEntity(
                                id = 0,
                                type = PropertyType.Flat.name,
                                price = 100000.toDouble(),
                                surface = 100.0,
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
                                /*photos = listOf(
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
                                    ),
                                    PhotoEntity(
                                        2,
                                        0,
                                        "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                        "Lounge 3"
                                    ),
                                    PhotoEntity(
                                        3,
                                        0,
                                        "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                        "Lounge 4"
                                    ),
                                    PhotoEntity(
                                        4,
                                        0,
                                        "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                        "Lounge 5"
                                    )
                                ),*/
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
                                entryDate = LocalDate.parse("2023-01-01"),
                                soldDate = LocalDate.parse("2023-01-02"),
                                agent = "John Doe"
                            ),
                            PropertyEntity(
                                id = 1,
                                type = PropertyType.House.name,
                                price = 200000.toDouble(),
                                surface = 200.0,
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
                                /*photos = listOf(
                                    PhotoEntity(
                                        0,
                                        0,
                                        "android.resource://com.bakjoul.realestatemanager/drawable/penthouse_upper_east_side",
                                        "Lounge"
                                    )
                                ),*/
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
                                entryDate = LocalDate.parse("2023-02-01"),
                                soldDate = null,
                                agent = "Jane Doe"
                            )
                        )
                    )

                    workManager.enqueue(
                        OneTimeWorkRequestBuilder<InitializeDatabaseWorker>()
                            .setInputData(workDataOf(InitializeDatabaseWorker.KEY_INPUT_DATA to propertiesAsJson))
                            .build()
                    )
                }
            })
            return builder.build()
        }
    }
}
