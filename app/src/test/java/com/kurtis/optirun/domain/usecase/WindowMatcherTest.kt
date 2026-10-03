package com.kurtis.optirun.domain.usecase

import com.kurtis.optirun.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class WindowMatcherTest {
    private val matcher = WindowMatcher()
    private val now = LocalDateTime.of(2026, 10, 3, 8, 0)

    private fun hour(h: Int, code: Int = 0, temp: Double = 12.0, rain: Int = 0, wind: Double = 5.0) =
        HourlyWeather(now.withHour(h), temp, rain, 0.0, code, wind)

    private fun prefs(w: WeatherPref, duration: Int = 1) =
        RunPreferences(w, TerrainPref.FLAT, durationHours = duration)

    @Test fun `sunny excludes rainy hours`() {
        val hours = listOf(hour(9, code = 0), hour(10, code = 61, rain = 80))
        val result = matcher.findWindows(hours, prefs(WeatherPref.SUNNY), now)
        assertEquals(listOf(9), result.map { it.start.hour })
    }

    @Test fun `rainy only returns rainy hours`() {
        val hours = listOf(hour(9, code = 0), hour(10, code = 61, rain = 80))
        val result = matcher.findWindows(hours, prefs(WeatherPref.RAINY), now)
        assertEquals(listOf(10), result.map { it.start.hour })
    }

    @Test fun `thunderstorms excluded even for any`() {
        val hours = listOf(hour(9, code = 95))
        assertTrue(matcher.findWindows(hours, prefs(WeatherPref.ANY), now).isEmpty())
    }

    @Test fun `past hours and night hours excluded`() {
        val hours = listOf(hour(7), hour(22), hour(9))
        val result = matcher.findWindows(hours, prefs(WeatherPref.ANY), now)
        assertEquals(listOf(9), result.map { it.start.hour })
    }

    @Test fun `better weather ranks first`() {
        val hours = listOf(hour(9, temp = 25.0), hour(11, temp = 12.0))
        val result = matcher.findWindows(hours, prefs(WeatherPref.ANY), now)
        assertEquals(11, result.first().start.hour)
    }

    @Test fun `multi-hour windows need consecutive matching hours and do not overlap`() {
        val hours = listOf(hour(9), hour(10), hour(11, code = 61), hour(12), hour(13))
        val result = matcher.findWindows(hours, prefs(WeatherPref.SUNNY, duration = 2), now)
        assertEquals(setOf(9, 12), result.map { it.start.hour }.toSet())
        assertTrue(result.all { it.hours.size == 2 })
    }
}