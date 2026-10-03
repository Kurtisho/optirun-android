package com.kurtis.optirun.domain.usecase

import com.kurtis.optirun.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class WindowMatcherTest {
    private val matcher = WindowMatcher()
    private val now = LocalDateTime.of(2026, 10, 3, 8, 0)

    private fun hour(h: Int, day: Int = 0, code: Int = 0, temp: Double = 12.0, rain: Int = 0, wind: Double = 5.0) =
        HourlyWeather(now.plusDays(day.toLong()).withHour(h), temp, rain, 0.0, code, wind)

    private fun prefs(w: WeatherPref, duration: Int = 1, horizon: Int = 7) =
        RunPreferences(w, TerrainPref.FLAT, durationHours = duration, horizonDays = horizon)

    @Test fun `sunny excludes rainy hours`() {
        val result = matcher.findWindows(listOf(hour(9), hour(10, code = 61, rain = 80)), prefs(WeatherPref.SUNNY), now)
        assertEquals(listOf(9), result.map { it.start.hour })
    }

    @Test fun `rainy only returns rainy hours`() {
        val result = matcher.findWindows(listOf(hour(9), hour(10, code = 61, rain = 80)), prefs(WeatherPref.RAINY), now)
        assertEquals(listOf(10), result.map { it.start.hour })
    }

    @Test fun `thunderstorms excluded even for any`() {
        assertTrue(matcher.findWindows(listOf(hour(9, code = 95)), prefs(WeatherPref.ANY), now).isEmpty())
    }

    @Test fun `past and night hours excluded`() {
        val result = matcher.findWindows(listOf(hour(7), hour(22), hour(9)), prefs(WeatherPref.ANY), now)
        assertEquals(listOf(9), result.map { it.start.hour })
    }

    @Test fun `better weather wins within a day`() {
        val result = matcher.findWindows(listOf(hour(9, temp = 25.0), hour(11)), prefs(WeatherPref.ANY), now)
        assertEquals(listOf(11), result.map { it.start.hour })
    }

    @Test fun `only one window per day`() {
        val result = matcher.findWindows(listOf(hour(9), hour(12), hour(9, day = 1)), prefs(WeatherPref.ANY), now)
        assertEquals(2, result.size)
        assertEquals(2, result.map { it.start.toLocalDate() }.toSet().size)
    }

    @Test fun `sooner day preferred when weather is equal`() {
        val result = matcher.findWindows(listOf(hour(9, day = 3), hour(9, day = 1)), prefs(WeatherPref.ANY), now)
        assertEquals(now.plusDays(1).toLocalDate(), result.first().start.toLocalDate())
    }

    @Test fun `hours beyond horizon excluded`() {
        val result = matcher.findWindows(listOf(hour(9, day = 8)), prefs(WeatherPref.ANY, horizon = 7), now)
        assertTrue(result.isEmpty())
    }

    @Test fun `multi-hour windows need consecutive matching hours`() {
        val hours = listOf(hour(9), hour(10, code = 61, rain = 80), hour(11), hour(12))
        val result = matcher.findWindows(hours, prefs(WeatherPref.SUNNY, duration = 2), now)
        assertEquals(listOf(11), result.map { it.start.hour })
        assertEquals(2, result.first().hours.size)
    }
}