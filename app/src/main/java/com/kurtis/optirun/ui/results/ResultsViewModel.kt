package com.kurtis.optirun.ui.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kurtis.optirun.data.location.LocationProvider
import com.kurtis.optirun.data.route.RouteRepository
import com.kurtis.optirun.data.weather.WeatherRepo
import com.kurtis.optirun.domain.model.*
import com.kurtis.optirun.domain.usecase.TerrainClassifier
import com.kurtis.optirun.domain.usecase.WindowMatcher
import com.kurtis.optirun.ui.ResultsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

sealed interface ResultsUiState {
    data object Loading : ResultsUiState
    data class Error(val msg: String) : ResultsUiState
    data class Success(
        val windows: List<RunWindow>,
        val route: Route?,
        val routeTerrain: TerrainPref?,
        val requestedTerrain: TerrainPref,
        val routeError: String?,
        val usingDefaultLocation: Boolean,
        val startLabel: String,
        val routeLoading: Boolean = false,
    ) : ResultsUiState
}

@HiltViewModel
class ResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val weatherRepo: WeatherRepo,
    private val routeRepo: RouteRepository,
    private val locationProvider: LocationProvider,
    private val matcher: WindowMatcher,
    private val classifier: TerrainClassifier,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<ResultsRoute>()
    private val prefs = RunPreferences(
        weather = WeatherPref.valueOf(args.weather),
        terrain = TerrainPref.valueOf(args.terrain),
        distanceKm = args.distanceKm.toDouble(),
    )

    private val chosenStart: LatLng? = run {
        val lat = args.startLat?.toDoubleOrNull()
        val lon = args.startLon?.toDoubleOrNull()
        if (lat != null && lon != null) LatLng(lat, lon) else null
    }

    private var routeStart: LatLng? = null
    private var seedOffset = 0

    private val _state = MutableStateFlow<ResultsUiState>(ResultsUiState.Loading)
    val state = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = ResultsUiState.Loading
        val here = chosenStart ?: locationProvider.current()
        val start = here ?: DEFAULT_LOCATION
        routeStart = start
        seedOffset = 0

        _state.value = try {
            coroutineScope {
                // Weather and routes are fetched at the same time
                val hoursJob = async { weatherRepo.hourly(start.lat, start.lon) }
                val routesJob = async { runCatching { routeRepo.loops(start, prefs.distanceKm, seedsFor(0)) } }

                val windows = matcher.findWindows(hoursJob.await(), prefs, LocalDateTime.now())
                val routes = routesJob.await()
                val best = routes.getOrNull()?.let { classifier.pickBest(it, prefs.terrain, prefs.distanceKm) }

                ResultsUiState.Success(
                    windows = windows,
                    route = best,
                    routeTerrain = best?.let { classifier.classify(it) },
                    requestedTerrain = prefs.terrain,
                    routeError = routes.exceptionOrNull()?.message,
                    usingDefaultLocation = here == null,
                    startLabel = args.startLabel ?: if (here != null) "Your location" else "Downtown Vancouver",
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultsUiState.Error(e.message ?: "Something went wrong")
        }
    }

    fun newRoute() {
        val current = _state.value as? ResultsUiState.Success ?: return
        val from = routeStart ?: return
        if (current.routeLoading) return

        seedOffset += SEEDS_PER_BATCH
        _state.value = current.copy(routeLoading = true)

        viewModelScope.launch {
            val routes = runCatching { routeRepo.loops(from, prefs.distanceKm, seedsFor(seedOffset)) }
            val best = routes.getOrNull()?.let { classifier.pickBest(it, prefs.terrain, prefs.distanceKm) }
            val shown = best ?: current.route   // keep the old route if the new search fails
            _state.value = current.copy(
                route = shown,
                routeTerrain = shown?.let { classifier.classify(it) },
                routeError = if (best == null) routes.exceptionOrNull()?.message ?: "No other routes found" else null,
                routeLoading = false,
            )
        }
    }

    private fun seedsFor(offset: Int) = (offset + 1..offset + SEEDS_PER_BATCH).toList()

    companion object {
        val DEFAULT_LOCATION = LatLng(49.2827, -123.1207)  // downtown Vancouver
        const val SEEDS_PER_BATCH = 3
    }
}