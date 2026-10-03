package com.kurtis.optirun.data.route

import com.kurtis.optirun.domain.model.LatLng
import com.kurtis.optirun.domain.model.Route
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepository @Inject constructor(private val api: OrsApi) {

    // Requests several loops at once
    suspend fun loops(start: LatLng, distanceKm: Double, seeds: List<Int> = listOf(1, 2, 3)): List<Route> =
        coroutineScope {
            val results = seeds.map { seed -> async { runCatching { fetch(start, distanceKm, seed) } } }.awaitAll()
            results.mapNotNull { it.getOrNull() }.ifEmpty {
                throw results.firstNotNullOf { it.exceptionOrNull() }
            }
        }

    private suspend fun fetch(start: LatLng, distanceKm: Double, seed: Int): Route {
        val request = OrsRequest(
            coordinates = listOf(listOf(start.lon, start.lat)),
            options = OrsOptions(RoundTrip(length = (distanceKm * 1000).toInt(), seed = seed)),
        )
        val f = api.roundTrip(request).features.first()
        return Route(
            distanceKm = f.properties.summary.distance / 1000,
            ascentM = f.properties.ascent,
            descentM = f.properties.descent,
            durationMin = f.properties.summary.duration / 60,
            path = f.geometry.coordinates.map { LatLng(lat = it[1], lon = it[0]) },
        )
    }
}