package com.kurtis.optirun.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kurtis.optirun.ui.home.HomeScreen
import com.kurtis.optirun.ui.results.ResultsScreen
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable data class ResultsRoute(val weather: String, val terrain: String, val distanceKm: Int)

@Composable
fun OptiRunNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(onFind = { p ->
                nav.navigate(ResultsRoute(p.weather.name, p.terrain.name, p.distanceKm.toInt()))
            })
        }
        composable<ResultsRoute> {
            ResultsScreen(onBack = { nav.popBackStack() })
        }
    }
}