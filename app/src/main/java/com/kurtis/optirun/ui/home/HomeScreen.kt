package com.kurtis.optirun.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kurtis.optirun.domain.model.Place
import com.kurtis.optirun.domain.model.RunPreferences
import com.kurtis.optirun.domain.model.TerrainPref
import com.kurtis.optirun.domain.model.WeatherPref
import kotlin.math.roundToInt

private enum class StartMode { MY_LOCATION, SEARCH }

private fun WeatherPref.label() = when (this) {
    WeatherPref.SUNNY -> "☀️ Sunny"
    WeatherPref.RAINY -> "🌧️ Rainy"
    WeatherPref.ANY -> "🌤️ Any"
}

private fun TerrainPref.label() = when (this) {
    TerrainPref.FLAT -> "🛤️ Flat"
    TerrainPref.HILLY -> "⛰️ Hilly"
}

@Composable
fun HomeScreen(onFind: (RunPreferences, Place?) -> Unit, vm: HomeViewModel = hiltViewModel()) {
    var weather by rememberSaveable { mutableStateOf(WeatherPref.ANY) }
    var terrain by rememberSaveable { mutableStateOf(TerrainPref.FLAT) }
    var distance by rememberSaveable { mutableFloatStateOf(5f) }
    var mode by rememberSaveable { mutableStateOf(StartMode.MY_LOCATION) }

    val query by vm.query.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val suggestions by vm.suggestions.collectAsStateWithLifecycle()

    fun prefs() = RunPreferences(weather, terrain, distanceKm = distance.roundToInt().toDouble())

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { onFind(prefs(), null) }

    Scaffold { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(
                    "OptiRun",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Find the best time and loop for your next run.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Section("Start from") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(mode == StartMode.MY_LOCATION, { mode = StartMode.MY_LOCATION }, { Text("📍 My location") })
                    FilterChip(mode == StartMode.SEARCH, { mode = StartMode.SEARCH }, { Text("🔎 Search a place") })
                }
                if (mode == StartMode.SEARCH) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = vm::onQueryChange,
                        label = { Text("Park, address, or landmark") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (selected == null) {
                        suggestions.forEach { place ->
                            Text(
                                "📍  ${place.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { vm.onSelect(place) }
                                    .padding(vertical = 10.dp),
                            )
                        }
                    }
                }
            }

            Section("Weather") {
                ChipRow(WeatherPref.entries, weather, label = { it.label() }, onSelect = { weather = it })
            }

            Section("Terrain") {
                ChipRow(TerrainPref.entries, terrain, label = { it.label() }, onSelect = { terrain = it })
            }

            Section("Distance") {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${distance.roundToInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        " km",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }
                Slider(value = distance, onValueChange = { distance = it }, valueRange = 2f..15f, steps = 12)
            }

            Button(
                onClick = {
                    if (mode == StartMode.SEARCH) onFind(prefs(), selected)
                    else permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                },
                enabled = mode == StartMode.MY_LOCATION || selected != null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text("Find my run", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun <T> ChipRow(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
            )
        }
    }
}