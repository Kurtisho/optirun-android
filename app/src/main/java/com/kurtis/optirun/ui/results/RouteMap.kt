package com.kurtis.optirun.ui.results

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.kurtis.optirun.R
import com.kurtis.optirun.domain.model.Route
import com.google.android.gms.maps.model.LatLng as MapLatLng

private val RoutePink = Color(0xFFF09494)

@Composable
fun RouteMap(route: Route, cameraState: CameraPositionState, modifier: Modifier = Modifier) {
    val points = remember(route) { route.path.map { MapLatLng(it.lat, it.lon) } }
    if (points.isEmpty()) return

    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val lightStyle = remember { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style) }

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
        properties = MapProperties(mapStyleOptions = if (dark) null else lightStyle),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
        onMapLoaded = { mapLoaded = true },
    ) {
        Polyline(points = points, color = RoutePink, width = 14f)
        Marker(
            state = startMarker,
            title = "Start / finish",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
        )
    }
}