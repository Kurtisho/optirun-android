package com.kurtis.optirun.data.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String =
            "temperature_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m",
        @Query("forecast_days") days: Int = 16,
        @Query("timezone") tz: String = "auto",
    ): ForecastResponse
}

@Serializable
data class ForecastResponse(val hourly: HourlyDto)

@Serializable
data class HourlyDto(
    val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double?>,
    @SerialName("precipitation_probability") val precipProb: List<Int?>,
    val precipitation: List<Double?>,
    @SerialName("weather_code") val weatherCode: List<Int?>,
    @SerialName("wind_speed_10m") val windSpeed: List<Double?>,
)