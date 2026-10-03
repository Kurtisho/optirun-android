package com.kurtis.optirun.domain.model

enum class WeatherPref { SUNNY, RAINY, ANY }
enum class TerrainPref { FLAT, HILLY }

data class RunPreferences(
    val weather: WeatherPref,
    val terrain: TerrainPref,
    val distanceKm: Double = 5.0,
    val durationHours: Int = 1,
    val earliestHour: Int = 6,   // no runs starting before 6am
    val latestHour: Int = 21,    // must finish by 9pm
    val horizonDays: Int = 7,
)