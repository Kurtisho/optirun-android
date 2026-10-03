package com.kurtis.optirun.domain.usecase

import com.kurtis.optirun.domain.model.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.abs
import java.time.Duration

class WindowMatcher @Inject constructor() {

    fun findWindows(
        hours: List<HourlyWeather>,
        prefs: RunPreferences,
        now: LocalDateTime,
        limit: Int = 5,
    ): List<RunWindow> {
        val currentHour = now.truncatedTo(ChronoUnit.HOURS)
        val horizonEnd = currentHour.plusDays(prefs.horizonDays.toLong())

        val candidates = hours
            .filter { !it.time.isBefore(currentHour) && it.time.isBefore(horizonEnd) }
            .sortedBy { it.time }
            .windowed(prefs.durationHours)
            .filter { w -> w.zipWithNext().all { (a, b) -> b.time == a.time.plusHours(1) } }
            .filter { w -> w.all { matches(it, prefs) } }
            .map { w ->
                val daysAhead = Duration.between(currentHour, w.first().time).toHours() / 24.0
                RunWindow(
                    start = w.first().time,
                    end = w.last().time.plusHours(1),
                    score = w.map { score(it, prefs) }.average() - daysAhead * DAY_PENALTY,
                    hours = w,
                )
            }
            .sortedByDescending { it.score }

        val picked = mutableListOf<RunWindow>()
        for (c in candidates) {
            if (picked.size == limit) break
            val sameDay = picked.any { it.start.toLocalDate() == c.start.toLocalDate() }
            val overlaps = picked.any { it.start < c.end && c.start < it.end }
            if (!sameDay && !overlaps) picked += c
        }
        return picked
    }

    internal fun matches(h: HourlyWeather, prefs: RunPreferences): Boolean {
        if (h.time.hour < prefs.earliestHour || h.time.hour >= prefs.latestHour) return false
        val code = h.weatherCode ?: return false
        if (code in THUNDER) return false
        return when (prefs.weather) {
            WeatherPref.SUNNY -> code in CLEAR && (h.precipProb ?: 100) <= 20
            WeatherPref.RAINY -> code in RAIN
            WeatherPref.ANY -> true
        }
    }

    internal fun score(h: HourlyWeather, prefs: RunPreferences): Double {
        val temp = h.tempC ?: IDEAL_TEMP_C
        val rain = h.precipProb ?: 0
        var s = 100.0 - abs(temp - IDEAL_TEMP_C) * 2 - (h.windKmh ?: 0.0) * 0.5
        s += when (prefs.weather) {
            WeatherPref.SUNNY -> -rain * 0.5
            WeatherPref.RAINY -> rain * 0.2
            WeatherPref.ANY -> -rain * 0.3
        }
        return s
    }

    companion object {
        const val IDEAL_TEMP_C = 12.0
        const val DAY_PENALTY = 3.0
        val CLEAR = 0..1
        val RAIN = (51..67).toSet() + (80..82)
        val THUNDER = 95..99
    }
}