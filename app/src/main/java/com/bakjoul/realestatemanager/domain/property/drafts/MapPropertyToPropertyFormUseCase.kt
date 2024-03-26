package com.bakjoul.realestatemanager.domain.property.drafts

import com.bakjoul.realestatemanager.domain.property.model.PropertyEntity
import com.bakjoul.realestatemanager.domain.property.model.PropertyTypeEntity
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormAddress
import com.bakjoul.realestatemanager.domain.property_form.model.PropertyFormEntity
import javax.inject.Inject

class MapPropertyToPropertyFormUseCase @Inject constructor() {
    fun invoke(property: PropertyEntity): PropertyFormEntity {
        return PropertyFormEntity(
            id = property.id,
            type = PropertyTypeEntity.values().find { it.name == property.type },
            isSold = property.saleDate != null,
            forSaleSince = property.forSaleSince,
            dateOfSale = property.saleDate,
            referencePrice = property.price,
            referenceSurface = property.surface,
            rooms = property.rooms,
            bathrooms = property.bathrooms,
            bedrooms = property.bedrooms,
            pointsOfInterest = property.amenities,
            autoCompleteAddress = PropertyFormAddress(
                streetNumber = property.address.streetNumber,
                route = property.address.route,
                zipcode = property.address.zipcode,
                city = property.address.city,
                state = property.address.state,
                country = property.address.country,
                latitude = property.address.latitude,
                longitude = property.address.longitude
            ),
            address = PropertyFormAddress(
                streetNumber = property.address.streetNumber,
                route = property.address.route,
                complementaryAddress = property.address.complementaryAddress,
                zipcode = property.address.zipcode,
                city = property.address.city,
                state = property.address.state,
                country = property.address.country,
                latitude = property.address.latitude,
                longitude = property.address.longitude
            ),
            description = property.description,
            photos = property.photos,
            featuredPhotoId = property.featuredPhotoId,
            agent = property.agent,
            lastUpdate = property.entryDate
        )
    }
}
