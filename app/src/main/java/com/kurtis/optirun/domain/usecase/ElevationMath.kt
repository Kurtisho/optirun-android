package com.kurtis.optirun.domain.usecase

object ElevationMath {
    // Counts climbing only when elevation rises at least `threshold` meters
    // above the last reference point, so small up-and-down noise is ignored.
    fun smoothedAscent(elevations: List<Double>, threshold: Double = 5.0): Double {
        if (elevations.size < 2) return 0.0
        var ref = elevations.first()
        var total = 0.0
        for (e in elevations) {
            val d = e - ref
            if (d >= threshold) { total += d; ref = e }
            else if (d <= -threshold) ref = e
        }
        return total
    }

    // Descent is just ascent walked backwards
    fun smoothedDescent(elevations: List<Double>, threshold: Double = 5.0): Double =
        smoothedAscent(elevations.reversed(), threshold)
}