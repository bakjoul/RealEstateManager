package com.bakjoul.realestatemanager.domain.geocoding

import android.util.Log
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingResultEntity
import com.bakjoul.realestatemanager.domain.geocoding.model.GeocodingWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAddressDetailsUseCase @Inject constructor(private val geocodingRepository: GeocodingRepository) {

    private companion object {
        private const val TAG = "GetAddressDetailsUC"
    }

    suspend fun invoke(placeId: String): Flow<GeocodingResultEntity> = flow {
        when (val wrapper = geocodingRepository.getAddressDetails(placeId)) {
            is GeocodingWrapper.Success -> emit(wrapper.results.first())
            is GeocodingWrapper.NoResults -> emit(wrapper.results.first())
            is GeocodingWrapper.Failure -> Log.i(TAG, "Failed to get address details")
            is GeocodingWrapper.Error -> Log.e(TAG, "Error while getting address details: ${wrapper.throwable.message}")
        }
    }
}
