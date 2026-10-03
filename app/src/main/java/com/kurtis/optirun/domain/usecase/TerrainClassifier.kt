package com.kurtis.optirun.domain.usecase

import com.kurtis.optirun.domain.model.Route
import com.kurtis.optirun.domain.model.TerrainPref
import javax.inject.Inject
import kotlin.math.abs
class TerrainClassifier @Inject constructor() {

    fun classify(route: Route): TerrainPref =
        if (route.ascentPerKm >= HILLY_THRESHOLD_M_PER_KM) TerrainPref.HILLY else TerrainPref.FLAT

    // Always returns the closest match, even if no route fully fits
    fun pickBest(routes: List<Route>, pref: TerrainPref, targetKm: Double? = null): Route? {
        val pool = if (targetKm == null) routes else {
            val near = routes.filter { abs(it.distanceKm - targetKm) / targetKm <= DISTANCE_TOLERANCE }
            near.ifEmpty { listOfNotNull(routes.minByOrNull { abs(it.distanceKm - targetKm) }) }
        }
        return when (pref) {
            TerrainPref.FLAT -> pool.minByOrNull { it.ascentPerKm }
            TerrainPref.HILLY -> pool.maxByOrNull { it.ascentPerKm }
        }
    }

    companion object {
        const val HILLY_THRESHOLD_M_PER_KM = 15.0
        const val DISTANCE_TOLERANCE = 0.25
    }
}