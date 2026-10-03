package com.kurtis.optirun.data.weather

import com.kurtis.optirun.domain.model.HourlyWeather
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepo @Inject constructor(private val api: OpenMeteoApi) {
    suspend fun hourly(lat: Double, lon: Double): List<HourlyWeather> {
        val h = api.forecast(lat, lon).hourly
        return h.time.indices.map { i ->
            HourlyWeather(
                time = LocalDateTime.parse(h.time[i]),
                tempC = h.temperature.getOrNull(i),
                precipProb = h.precipProb.getOrNull(i),
                precipMm = h.precipitation.getOrNull(i),
                weatherCode = h.weatherCode.getOrNull(i),
                windKmh = h.windSpeed.getOrNull(i),
            )
        }
    }
}