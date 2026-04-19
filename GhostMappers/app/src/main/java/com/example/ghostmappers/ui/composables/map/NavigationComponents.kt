package com.example.ghostmappers.ui.composables.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions


class NavigationComponents(context: Context) {
    val mapboxNavigation: MapboxNavigation = MapboxNavigationProvider.create(
        NavigationOptions.Builder(context).build()
    )

    val locationProvider = NavigationLocationProvider()

    val routeLineApi = MapboxRouteLineApi(
        MapboxRouteLineApiOptions.Builder().build()
    )

    val routeLineView = MapboxRouteLineView(
        MapboxRouteLineViewOptions.Builder(context).build()
    )
}

@Composable
fun rememberNavigationComponents(context: Context) = remember {
    NavigationComponents(context)
}