package com.kurtis.optirun.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kurtis.optirun.domain.model.RunPreferences
import com.kurtis.optirun.domain.model.TerrainPref
import com.kurtis.optirun.domain.model.WeatherPref
import kotlin.math.roundToInt

@Composable
fun HomeScreen(onFind: (RunPreferences) -> Unit) {
    var weather by rememberSaveable { mutableStateOf(WeatherPref.ANY) }
    var terrain by rememberSaveable { mutableStateOf(TerrainPref.FLAT) }
    var distance by rememberSaveable { mutableFloatStateOf(5f) }

    // Ask for location, then continue whether or not it was granted
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onFind(RunPreferences(weather, terrain, distanceKm = distance.roundToInt().toDouble()))
    }

    Scaffold { padding ->
        Column(
            Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("OptiRun", style = MaterialTheme.typography.headlineLarge)

            Text("Weather", style = MaterialTheme.typography.titleMedium)
            ChipRow(WeatherPref.entries, weather) { weather = it }

            Text("Terrain", style = MaterialTheme.typography.titleMedium)
            ChipRow(TerrainPref.entries, terrain) { terrain = it }

            Text("Distance: ${distance.roundToInt()} km", style = MaterialTheme.typography.titleMedium)
            Slider(value = distance, onValueChange = { distance = it }, valueRange = 2f..15f, steps = 12)

            Button(
                onClick = {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                },
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