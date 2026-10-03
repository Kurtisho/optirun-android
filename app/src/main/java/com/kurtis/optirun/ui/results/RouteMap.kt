package com.kurtis.optirun.ui.results

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.kurtis.optirun.domain.model.Route
import com.google.android.gms.maps.model.LatLng as MapLatLng

private val RouteOrange = Color(0xFFFC4C02)

@Composable
fun RouteMap(route: Route, cameraState: CameraPositionState, modifier: Modifier = Modifier) {
    val points = remember(route) { route.path.map { MapLatLng(it.lat, it.lon) } }
    if (points.isEmpty()) return

    var mapLoaded by remember { mutableStateOf(false) }
    val startMarker = remember(points) { MarkerState(position = points.first()) }

    // Re-fit the camera whenever the map finishes loading or a new route arrives
    LaunchedEffect(points, mapLoaded) {
        if (mapLoaded) {
            val bounds = LatLngBounds.builder().apply { points.forEach { include(it) } }.build()
            cameraState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 80))
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
        onMapLoaded = { mapLoaded = true },
    ) {
        Polyline(points = points, color = RouteOrange, width = 12f)
        Marker(state = startMarker, title = "Start / finish")
    }
}