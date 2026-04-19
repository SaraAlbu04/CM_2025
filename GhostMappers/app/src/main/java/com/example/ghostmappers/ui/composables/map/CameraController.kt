package com.example.ghostmappers.ui.composables.map

import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera

class CameraController(
    private val state: MapState,
    private val navComponents: NavigationComponents
) {
    fun recenter() {
        val userLocation = state.userLocation ?: return
        val currentRoutes = navComponents.mapboxNavigation.getNavigationRoutes()

        if (currentRoutes.isNotEmpty()) {
            recenterDuringNavigation()
        } else {
            recenterDuringExploration(userLocation)
        }
    }

    private fun recenterDuringNavigation() {
        state.isUserControllingCamera = false

        val resetOptions = CameraOptions.Builder()
            .zoom(16.0)
            .pitch(0.0)
            .build()

        state.mapView?.mapboxMap?.setCamera(resetOptions)
        state.navigationCamera?.requestNavigationCameraToFollowing()
    }

    private fun recenterDuringExploration(userLocation: Point) {
        state.navigationCamera?.requestNavigationCameraToIdle()

        val cameraOptions = CameraOptions.Builder()
            .center(userLocation)
            .zoom(16.0)
            .pitch(0.0)
            .bearing(0.0)
            .padding(EdgeInsets(0.0, 0.0, 0.0, 0.0))
            .build()

        val animationOptions = MapAnimationOptions.Builder()
            .duration(1000)
            .build()

        state.mapView?.camera?.flyTo(cameraOptions, animationOptions)
        state.isUserControllingCamera = false
    }
}