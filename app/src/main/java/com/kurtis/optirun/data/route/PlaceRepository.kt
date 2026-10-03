package com.kurtis.optirun.data.route

import com.kurtis.optirun.domain.model.LatLng
import com.kurtis.optirun.domain.model.Place
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepository @Inject constructor(private val api: OrsApi) {
    suspend fun search(text: String, near: LatLng?): List<Place> =
        api.autocomplete(text, near?.lon, near?.lat).features.map {
            Place(it.properties.label, LatLng(lat = it.geometry.coordinates[1], lon = it.geometry.coordinates[0]))
        }
}