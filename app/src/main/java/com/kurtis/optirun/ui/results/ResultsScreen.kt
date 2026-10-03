package com.kurtis.optirun.ui.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kurtis.optirun.domain.model.RunWindow
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState

private val dayFmt = DateTimeFormatter.ofPattern("EEE MMM d, h:mm a")
private val timeFmt = DateTimeFormatter.ofPattern("h:mm a")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(onBack: () -> Unit, vm: ResultsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your best runs") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                ResultsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ResultsUiState.Error -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Error: ${s.msg}")
                    Button(onClick = { vm.load() }) { Text("Retry") }
                }
                is ResultsUiState.Success -> ResultsList(s, onNewRoute = vm::newRoute)
            }
        }
    }
}

@Composable
private fun ResultsList(s: ResultsUiState.Success, onNewRoute: () -> Unit) {
    val cameraState = rememberCameraPositionState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !cameraState.isMoving,   // the list pauses while the map is dragged
    ) {
        item {
            Text("Starting from: ${s.startLabel}", style = MaterialTheme.typography.titleSmall)
            if (s.usingDefaultLocation) {
                Text("Location unavailable, using default.", style = MaterialTheme.typography.bodySmall)
            }
        }
        item { RouteCard(s, cameraState, onNewRoute) }
        item { Text("Best time windows", style = MaterialTheme.typography.titleMedium) }
        if (s.windows.isEmpty()) item { Text("No upcoming hours match. Try \"Any\" weather.") }
        items(s.windows) { WindowCard(it) }
        item {
            Text(
                "Weather data by Open-Meteo.com (CC BY 4.0). Routes by openrouteservice.org, " +
                        "map data © OpenStreetMap contributors.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RouteCard(s: ResultsUiState.Success, cameraState: CameraPositionState, onNewRoute: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Suggested loop", style = MaterialTheme.typography.titleMedium)
            val r = s.route
            if (r == null) {
                Text("Route unavailable: ${s.routeError ?: "no routes found"}")
            } else {
                RouteMap(
                    r,
                    cameraState,
                    Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(12.dp)),
                )
                Spacer(Modifier.height(8.dp))
                Text("Distance: %.1f km".format(r.distanceKm))
                Text("Climb: %.0f m (%.0f m/km)".format(r.ascentM, r.ascentPerKm))
                val label = s.routeTerrain?.name?.lowercase() ?: "?"
                val note = if (s.routeTerrain != s.requestedTerrain) " (closest match nearby)" else ""
                Text("Terrain: $label$note")
                s.routeError?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            OutlinedButton(
                onClick = onNewRoute,
                enabled = !s.routeLoading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (s.routeLoading) "Finding a new loop…" else "New route") }
        }
    }
}

@Composable
private fun WindowCard(w: RunWindow) {
    val h = w.hours.first()
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("${w.start.format(dayFmt)} – ${w.end.format(timeFmt)}", style = MaterialTheme.typography.titleSmall)
            Text(
                "${h.tempC?.roundToInt() ?: "?"}°C · rain ${h.precipProb ?: "?"}% · " +
                        "wind ${h.windKmh?.roundToInt() ?: "?"} km/h"
            )
        }
    }
}