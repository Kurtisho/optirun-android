package com.kurtis.optirun.domain.model

import java.time.LocalDateTime

data class HourlyWeather(
    val time: LocalDateTime,
    val tempC: Double?,
    val precipProb: Int?,
    val precipMm: Double?,
    val weatherCode: Int?,
    val windKmh: Double?,
)