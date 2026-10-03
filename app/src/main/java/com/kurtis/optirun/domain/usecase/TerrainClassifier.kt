package com.kurtis.optirun.domain.usecase

import com.kurtis.optirun.domain.model.Route
import com.kurtis.optirun.domain.model.TerrainPref
import javax.inject.Inject

class TerrainClassifier @Inject constructor() {

    fun classify(route: Route): TerrainPref =
        if (route.ascentPerKm >= HILLY_THRESHOLD_M_PER_KM) TerrainPref.HILLY else TerrainPref.FLAT

    // Always returns the closest match, even if no route fully fits
    fun pickBest(routes: List<Route>, pref: TerrainPref): Route? = when (pref) {
        TerrainPref.FLAT -> routes.minByOrNull { it.ascentPerKm }
        TerrainPref.HILLY -> routes.maxByOrNull { it.ascentPerKm }
    }

    companion object {
        const val HILLY_THRESHOLD_M_PER_KM = 15.0
    }
}