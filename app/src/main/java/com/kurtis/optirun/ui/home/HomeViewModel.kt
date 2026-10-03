package com.kurtis.optirun.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurtis.optirun.data.weather.WeatherRepo
import com.kurtis.optirun.domain.model.HourlyWeather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val hours: List<HourlyWeather>) : HomeUiState
    data class Error(val msg: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: WeatherRepo) : ViewModel() {
    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state = _state.asStateFlow()

    init { load(49.2827, -123.1207) }

    fun load(lat: Double, lon: Double) = viewModelScope.launch {
        _state.value = HomeUiState.Loading
        _state.value = runCatching { repo.hourly(lat, lon) }.fold(
            { HomeUiState.Success(it) },
            { HomeUiState.Error(it.message ?: "Unknown error") },
        )
    }
}