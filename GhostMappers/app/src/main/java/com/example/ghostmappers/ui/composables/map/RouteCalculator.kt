package com.example.ghostmappers.ui.composables.map

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.ghostmappers.data.model.Ghost
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure

class RouteCalculator(
    private val context: Context,
    private val state: MapState,
    private val navComponents: NavigationComponents
) {
    fun calculateRoute(target: Ghost, start: Point) {
        if (state.lastRequestedGhostId == target.id) return

        val end = Point.fromLngLat(target.location!!.longitude, target.location.latitude)

        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .coordinatesList(listOf(start, end))
            .build()

        navComponents.mapboxNavigation.requestRoutes(
            routeOptions,
            object : NavigationRouterCallback {
                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                    state.lastRequestedGhostId = target.id
                    navComponents.mapboxNavigation.setNavigationRoutes(routes)
                    state.isUserControllingCamera = false

                    val flatCamera = CameraOptions.Builder()
                        .pitch(0.0)
                        .zoom(16.0)
                        .build()

                    state.mapView?.mapboxMap?.setCamera(flatCamera)
                    state.navigationCamera?.requestNavigationCameraToFollowing()
                    state.viewportDataSource?.evaluate()
                    state.selectedGhostId = null
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    Log.e("MapDebug", "Route failed: $reasons")
                    Toast.makeText(context, "Failed to find route: $reasons", Toast.LENGTH_LONG)
                        .show()
                }

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {
                    Log.w("MapDebug", "Route request canceled")
                }
            }
        )
    }
}