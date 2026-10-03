package com.kurtis.optirun.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kurtis.optirun.ui.home.HomeScreen
import com.kurtis.optirun.ui.results.ResultsScreen
import kotlinx.serialization.Serializable

@Serializable object HomeRoute

@Serializable
data class ResultsRoute(
    val weather: String,
    val terrain: String,
    val distanceKm: Int,
    val startLat: String? = null,
    val startLon: String? = null,
    val startLabel: String? = null,
)

@Composable
fun OptiRunNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(onFind = { p, place ->
                nav.navigate(
                    ResultsRoute(
                        weather = p.weather.name,
                        terrain = p.terrain.name,
                        distanceKm = p.distanceKm.toInt(),
                        startLat = place?.location?.lat?.toString(),
                        startLon = place?.location?.lon?.toString(),
                        startLabel = place?.label,
                    )
                )
            })
        }
        composable<ResultsRoute> {
            ResultsScreen(onBack = { nav.popBackStack() })
        }
    }
}