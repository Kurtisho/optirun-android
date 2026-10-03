package com.kurtis.optirun.ui.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(vm: HomeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    when (val s = state) {
        HomeUiState.Loading -> CircularProgressIndicator()
        is HomeUiState.Error -> Text("Error: ${s.msg}")
        is HomeUiState.Success -> LazyColumn {
            items(s.hours) { h ->
                Text("${h.time}  ${h.tempC}°C  rain ${h.precipProb}%  code ${h.weatherCode}")
            }
        }
    }
}