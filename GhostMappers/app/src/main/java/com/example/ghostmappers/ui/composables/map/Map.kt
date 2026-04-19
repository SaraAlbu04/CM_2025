package com.example.ghostmappers.ui.composables.map

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ghostmappers.data.GhostRepository
import com.example.ghostmappers.data.model.Ghost
import com.example.ghostmappers.handlers.handleKillGhost
import com.example.ghostmappers.services.GhostForegroundService
import com.example.ghostmappers.ui.composables.GhostBottomSheet
import com.example.ghostmappers.ui.composables.VacuumKill
import com.example.ghostmappers.ui.theme.Orange
import com.example.ghostmappers.utils.distanceMeters
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun Map(modifier: Modifier = Modifier) {
    val ghostRepository = remember { GhostRepository() }
    val ghosts by ghostRepository.getGhostsFlow().collectAsState(initial = emptyList())

    MapContent(modifier, ghosts)
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPreviewMapboxNavigationAPI::class)
@Composable
private fun MapContent(modifier: Modifier = Modifier, ghosts: List<Ghost>) {
    val context = LocalContext.current

    val scope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val intent = Intent(context, GhostForegroundService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }
    val state = rememberMapState()
    var markerManager by remember { mutableStateOf<GhostMarkerManager?>(null) }
    val navComponents = rememberNavigationComponents(context)
    val sheetState = rememberModalBottomSheetState()

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(-8.6291, 41.1579))
            zoom(15.0)
            pitch(0.0)
        }
    }

    val routeCalculator = remember { RouteCalculator(context, state, navComponents) }
    val cameraController = remember { CameraController(state, navComponents) }

    val selectedGhost = remember(state.selectedGhostId) {
        ghosts.find { it.id == state.selectedGhostId }
    }

    var ghostToKill by remember { mutableStateOf<Ghost?>(null) }

    // Handle route calculation
    LaunchedEffect(state.ghostForDirections, state.userLocation) {
        val target = state.ghostForDirections
        val start = state.userLocation

        if (target != null && start != null) {
            routeCalculator.calculateRoute(target, start)
        }
    }

    // Bottom sheet for ghost details
    if (selectedGhost != null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        ModalBottomSheet(
            onDismissRequest = { state.selectedGhostId = null },
            sheetState = sheetState
        ) {
            GhostBottomSheet(
                ghost = selectedGhost,
                distanceToGhost = if (state.userLocation != null)
                    distanceMeters(
                        state.userLocation!!.latitude(),
                        state.userLocation!!.longitude(),
                        selectedGhost.location!!.latitude,
                        selectedGhost.location.longitude
                    )
                else
                    Double.MAX_VALUE,
                onDirectionsClick = {
                    state.ghostForDirections = selectedGhost
                    state.selectedGhostId = null
                },
                onEnterKillMode = {
                    state.selectedGhostId = null
                    ghostToKill = selectedGhost
                },
                userId = userId
            )
        }
    }

    // Map
    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        scaleBar = {},
        compass = {}
    ) {
        // Initialize map
        MapEffect(Unit) { mapView ->
            val initializer = MapInitializer(context, state, navComponents)
            initializer.initialize(mapView)

            markerManager = GhostMarkerManager(mapView)
        }

        // Add ghost markers
        MapEffect(ghosts, markerManager) {
            markerManager?.updateMarkers(ghosts) { ghostId ->
                state.selectedGhostId = ghostId
            }
        }
    }

    //Kill ghost zoom in
    LaunchedEffect(ghostToKill) {
        ghostToKill?.location?.let { geo ->
            val point = Point.fromLngLat(ghostToKill!!.location!!.longitude, ghostToKill!!.location!!.latitude)
            mapViewportState.flyTo(
                com.mapbox.maps.CameraOptions.Builder()
                    .center(point)
                    .zoom(18.0)
                    .build(),
                com.mapbox.maps.plugin.animation.MapAnimationOptions.mapAnimationOptions { duration(2000) }
            )
        }
    }

    if (state.ghostForDirections != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Button(
                onClick = {
                    navComponents.mapboxNavigation.setNavigationRoutes(emptyList())
                    state.ghostForDirections = null
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 35.dp, start = 16.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = Color.White
                ),
            ) {
                Text("Cancel")
            }
        }
    }

    if (ghostToKill != null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

        VacuumKill(
            onCancel = { ghostToKill = null },
            onKillConfirmed = {
                // Perform the actual API call here
                scope.launch(Dispatchers.IO) {
                    val result = handleKillGhost(userId, ghostToKill!!.id)

                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            Toast.makeText(context, "Ghost Vacuumed Up!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Ghost escaped...", Toast.LENGTH_SHORT).show()
                        }
                        // Exit kill mode
                        ghostToKill = null
                    }
                }
            }
        )
    }

    // Recenter button
    if (state.userLocation != null) {
        RecenterButton { cameraController.recenter() }
    }
}

@Composable
fun RecenterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            modifier = Modifier.padding(end = 16.dp, bottom = 160.dp),
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                contentDescription = "Recenter"
            )
        }
    }
}