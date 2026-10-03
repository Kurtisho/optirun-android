package com.kurtis.optirun.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurtis.optirun.data.location.LocationProvider
import com.kurtis.optirun.data.route.PlaceRepository
import com.kurtis.optirun.domain.model.LatLng
import com.kurtis.optirun.domain.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val places: PlaceRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    val query = MutableStateFlow("")
    val selected = MutableStateFlow<Place?>(null)
    private var near: LatLng? = null

    init { viewModelScope.launch { near = locationProvider.current() } }

    // Waits 300ms after typing stops, needs 3+ characters, cancels outdated searches
    val suggestions: StateFlow<List<Place>> = query
        .debounce(300)
        .map { it.trim() }
        .distinctUntilChanged()
        .mapLatest { q ->
            if (q.length < 3) emptyList()
            else runCatching { places.search(q, near) }.getOrDefault(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(q: String) { query.value = q; selected.value = null }
    fun onSelect(p: Place) { selected.value = p; query.value = p.label }
}