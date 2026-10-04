package com.kurtis.optirun.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.kurtis.optirun.domain.model.RunWindow
import com.kurtis.optirun.ui.weatherEmoji
import com.kurtis.optirun.ui.weatherLabel
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val dayFmt = DateTimeFormatter.ofPattern("EEEE, MMM d")
private val timeFmt = DateTimeFormatter.ofPattern("h:mm a")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(onBack: () -> Unit, vm: ResultsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your run plan", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Back") } },
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                ResultsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ResultsUiState.Error -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Something went wrong", style = MaterialTheme.typography.titleMedium)
                    Text(s.msg, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !cameraState.isMoving,   // the list pauses while the map is dragged
    ) {
        item {
            Text("📍 ${s.startLabel}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            if (s.usingDefaultLocation) {
                Text(
                    "Location unavailable, using default.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { RouteCard(s, cameraState, onNewRoute) }
        item {
            Text(
                "Best time windows",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (s.windows.isEmpty()) item {
            Text(
                "No upcoming hours match. Try \"Any\" weather.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        itemsIndexed(s.windows) { i, w -> WindowCard(w, best = i == 0) }
        item {
            Text(
                "Weather data by Open-Meteo.com (CC BY 4.0). Routes by openrouteservice.org, " +
                        "map data © OpenStreetMap contributors.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun RouteCard(s: ResultsUiState.Success, cameraState: CameraPositionState, onNewRoute: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Suggested loop", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            val r = s.route
            if (r == null) {
                Text(
                    "Route unavailable: ${s.routeError ?: "no routes found"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                RouteMap(
                    r,
                    cameraState,
                    Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(16.dp)),
                )
                val terrain = s.routeTerrain?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "?"
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Stat("%.1f km".format(r.distanceKm), "Distance", Modifier.weight(1f))
                    Stat("%.0f m".format(r.ascentM), "Climb", Modifier.weight(1f))
                    Stat(terrain, "Terrain", Modifier.weight(1f))
                }
                if (s.routeTerrain != s.requestedTerrain) {
                    Text(
                        "No ${s.requestedTerrain.name.lowercase()} loop nearby, showing the closest match.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                s.routeError?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            FilledTonalButton(
                onClick = onNewRoute,
                enabled = !s.routeLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (s.routeLoading) "Finding a new loop…" else "🔄 New route") }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WindowCard(w: RunWindow, best: Boolean) {
    val h = w.hours.first()
    val colors = if (best) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else CardDefaults.cardColors()

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = colors) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(weatherEmoji(h.weatherCode), fontSize = if (best) 40.sp else 30.sp)
            Column(Modifier.weight(1f)) {
                if (best) {
                    Text(
                        "BEST PICK",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(w.start.format(dayFmt), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${w.start.format(timeFmt)} – ${w.end.format(timeFmt)}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${weatherLabel(h.weatherCode)} · ${h.tempC?.roundToInt() ?: "?"}°C · " +
                            "rain ${h.precipProb ?: "?"}% · wind ${h.windKmh?.roundToInt() ?: "?"} km/h",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (best) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}