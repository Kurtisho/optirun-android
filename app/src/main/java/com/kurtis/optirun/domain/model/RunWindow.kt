package com.kurtis.optirun.domain.model

import java.time.LocalDateTime

data class RunWindow(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val score: Double,
    val hours: List<HourlyWeather>,
)