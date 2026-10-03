package com.kurtis.optirun.domain.model

data class LatLng(val lat: Double, val lon: Double)

data class Route(
    val distanceKm: Double,
    val ascentM: Double,
    val descentM: Double,
    val durationMin: Double,
    val path: List<LatLng>,
) {
    val ascentPerKm: Double get() = if (distanceKm > 0) ascentM / distanceKm else 0.0
}