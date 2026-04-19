package com.example.ghostmappers.ui.composables.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ghostmappers.data.model.Ghost
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource

class MapState {
    var userLocation by mutableStateOf<Point?>(null)
    var hasInitialZoom by mutableStateOf(false)
    var isUserControllingCamera by mutableStateOf(true)
    var selectedGhostId by mutableStateOf<String?>(null)
    var ghostForDirections by mutableStateOf<Ghost?>(null)
    var lastRequestedGhostId by mutableStateOf<String?>(null)
    var mapView by mutableStateOf<MapView?>(null)
    var viewportDataSource by mutableStateOf<MapboxNavigationViewportDataSource?>(null)
    var navigationCamera by mutableStateOf<NavigationCamera?>(null)
}

@Composable
fun rememberMapState() = remember { MapState() }