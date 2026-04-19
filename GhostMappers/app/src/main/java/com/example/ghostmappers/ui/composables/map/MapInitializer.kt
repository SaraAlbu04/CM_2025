package com.example.ghostmappers.ui.composables.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource

class MapInitializer(
    private val context: Context,
    private val state: MapState,
    private val navComponents: NavigationComponents
) {
    fun initialize(mapView: MapView) {
        state.mapView = mapView

        setupViewportDataSource(mapView)
        setupNavigationCamera(mapView)
        setupGestureListeners(mapView)
        registerObservers()
        setupLocationPuck(mapView)
        startTripSession()
    }

    private fun setupViewportDataSource(mapView: MapView) {
        val displayMetrics = context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels.toDouble()

        state.viewportDataSource = MapboxNavigationViewportDataSource(mapView.mapboxMap).apply {
            followingPadding = EdgeInsets(
                screenHeight * 0.4, 50.0, screenHeight * 0.4, 50.0
            )

            options.followingFrameOptions.apply {
                defaultPitch = 0.0
                zoomUpdatesAllowed = false
                pitchUpdatesAllowed = true
                bearingUpdatesAllowed = true
            }
        }
    }

    private fun setupNavigationCamera(mapView: MapView) {
        state.navigationCamera = NavigationCamera(
            mapView.mapboxMap,
            mapView.camera,
            state.viewportDataSource!!
        )
    }

    private fun setupGestureListeners(mapView: MapView) {
        setupGestureListeners(
            mapView = mapView,
            navigationCamera = state.navigationCamera,
            onUserControlDetected = { state.isUserControllingCamera = true }
        )
    }

    private fun registerObservers() {
        val routesObserver = createRoutesObserver(state, navComponents)
        val locationObserver = createLocationObserver(state, navComponents)

        navComponents.mapboxNavigation.registerRoutesObserver(routesObserver)
        navComponents.mapboxNavigation.registerLocationObserver(locationObserver)
    }

    private fun setupLocationPuck(mapView: MapView) {
        mapView.location.apply {
            setLocationProvider(navComponents.locationProvider)
            enabled = true
            locationPuck = createDefault2DPuck(withBearing = false)
        }
    }

    private fun startTripSession() {
        val permissionState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            Log.d("MapDebug", "Permissions granted. Starting Trip Session...")
            navComponents.mapboxNavigation.startTripSession()
        } else {
            Log.e("MapDebug", "Permissions MISSING. Trip Session NOT started.")
        }
    }
}