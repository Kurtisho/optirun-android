package com.kurtis.optirun.data.route

import com.kurtis.optirun.domain.model.LatLng
import com.kurtis.optirun.domain.model.Route
import com.kurtis.optirun.domain.usecase.ElevationMath
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class RouteRepository @Inject constructor(private val api: OrsApi) {

    // Requests several loops at once
    suspend fun loops(start: LatLng, distanceKm: Double, seeds: List<Int> = (1..5).toList()): List<Route> {
        val first = fetchAll(start, distanceKm, seeds)
        val avgKm = first.map { it.distanceKm }.average()
        if (avgKm <= 0 || abs(avgKm - distanceKm) / distanceKm <= 0.25) return first

        // ORS missed the target: retry once with a length scaled to compensate
        val corrected = distanceKm * (distanceKm / avgKm)
        val second = runCatching { fetchAll(start, corrected, seeds) }.getOrDefault(emptyList())
        return first + second
    }
    private suspend fun fetchAll(start: LatLng, requestKm: Double, seeds: List<Int>): List<Route> =
        coroutineScope {
            val results = seeds.map { seed -> async { runCatching { fetch(start, requestKm, seed) } } }.awaitAll()
            results.mapNotNull { it.getOrNull() }.ifEmpty {
                throw results.firstNotNullOf { it.exceptionOrNull() }
            }
        }

    private suspend fun fetch(start: LatLng, requestKm: Double, seed: Int): Route {

        val request = OrsRequest(
            coordinates = listOf(listOf(start.lon, start.lat)),
            options = OrsOptions(RoundTrip(length = (requestKm * 1000).toInt(), seed = seed)),
        )
        val f = api.roundTrip(request).features.first()
        val elevations = f.geometry.coordinates.mapNotNull { it.getOrNull(2) }
        val hasElevation = elevations.size >= 2
        return Route(
            distanceKm = f.properties.summary.distance / 1000,
            ascentM = if (hasElevation) ElevationMath.smoothedAscent(elevations) else f.properties.ascent,
            descentM = if (hasElevation) ElevationMath.smoothedDescent(elevations) else f.properties.descent,
            durationMin = f.properties.summary.duration / 60,
            path = f.geometry.coordinates.map { LatLng(lat = it[1], lon = it[0]) },
        )
    }
}