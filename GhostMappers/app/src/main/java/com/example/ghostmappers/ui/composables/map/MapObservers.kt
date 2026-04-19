package com.example.ghostmappers.ui.composables.map

import android.util.Log
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver


fun createRoutesObserver(
    state: MapState,
    navComponents: NavigationComponents
): RoutesObserver {
    return RoutesObserver { routeUpdate ->
        val mv = state.mapView ?: return@RoutesObserver

        if (routeUpdate.navigationRoutes.isNotEmpty()) {
            navComponents.routeLineApi.setNavigationRoutes(routeUpdate.navigationRoutes) { drawData ->
                mv.mapboxMap.style?.let { style ->
                    navComponents.routeLineView.renderRouteDrawData(style, drawData)
                }
            }
            state.viewportDataSource?.onRouteChanged(routeUpdate.navigationRoutes.first())
        } else {
            navComponents.routeLineApi.setNavigationRoutes(emptyList()) { drawData ->
                mv.mapboxMap.style?.let { style ->
                    navComponents.routeLineView.renderRouteDrawData(style, drawData)
                }
            }
        }
    }
}


fun createLocationObserver(
    state: MapState,
    navComponents: NavigationComponents
): LocationObserver {
    return object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {}

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhanced = locationMatcherResult.enhancedLocation

            // Update location puck and user position
            navComponents.locationProvider.changePosition(
                location = enhanced,
                keyPoints = locationMatcherResult.keyPoints
            )

            state.userLocation = Point.fromLngLat(enhanced.longitude, enhanced.latitude)
            state.viewportDataSource?.onLocationChanged(enhanced)

            // Initial camera zoom on first location
            if (!state.hasInitialZoom) {
                state.hasInitialZoom = true
                Log.d("CameraDebug", "First location received.")

                val cameraOptions = CameraOptions.Builder()
                    .center(Point.fromLngLat(enhanced.longitude, enhanced.latitude))
                    .zoom(16.0)
                    .pitch(0.0)
                    .build()

                state.mapView?.mapboxMap?.setCamera(cameraOptions)
            }
        }
    }
}
