package com.kurtis.optirun.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kurtis.optirun.domain.model.Place
import com.kurtis.optirun.domain.model.RunPreferences
import com.kurtis.optirun.domain.model.TerrainPref
import com.kurtis.optirun.domain.model.WeatherPref
import kotlin.math.roundToInt

private enum class StartMode { MY_LOCATION, SEARCH }

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
            Modifier.padding(padding).padding(24.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("OptiRun", style = MaterialTheme.typography.headlineLarge)

            Text("Start from", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(mode == StartMode.MY_LOCATION, { mode = StartMode.MY_LOCATION }, { Text("My location") })
                FilterChip(mode == StartMode.SEARCH, { mode = StartMode.SEARCH }, { Text("Search a place") })
            }
            if (mode == StartMode.SEARCH) {
                OutlinedTextField(
                    value = query,
                    onValueChange = vm::onQueryChange,
                    label = { Text("Park, address, or landmark") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (selected == null) {
                    suggestions.forEach { place ->
                        Text(
                            place.label,
                            Modifier.fillMaxWidth().clickable { vm.onSelect(place) }.padding(vertical = 8.dp),
                        )
                    }
                }
            }

            Text("Weather", style = MaterialTheme.typography.titleMedium)
            ChipRow(WeatherPref.entries, weather) { weather = it }

            Text("Terrain", style = MaterialTheme.typography.titleMedium)
            ChipRow(TerrainPref.entries, terrain) { terrain = it }

            Text("Distance: ${distance.roundToInt()} km", style = MaterialTheme.typography.titleMedium)
            Slider(value = distance, onValueChange = { distance = it }, valueRange = 2f..15f, steps = 12)

            Button(
                onClick = {
                    if (mode == StartMode.SEARCH) onFind(prefs(), selected)
                    else permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                },
                enabled = mode == StartMode.MY_LOCATION || selected != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Find my run") }
        }
    }
}

@Composable
private fun <T : Enum<T>> ChipRow(options: List<T>, selected: T, onSelect: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
            )
        }
    }
}