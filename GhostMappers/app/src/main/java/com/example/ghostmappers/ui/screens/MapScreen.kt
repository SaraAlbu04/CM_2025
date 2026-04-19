package com.example.ghostmappers.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.ghostmappers.services.LocationErrorScreen
import com.example.ghostmappers.services.LocationPermissionManager
import com.example.ghostmappers.ui.composables.NavBar
import com.example.ghostmappers.ui.composables.map.Map
import com.example.ghostmappers.ui.navigation.Screens
import com.example.ghostmappers.ui.views.UserViewModel

@Composable
fun MapScreen(navController: NavController, userViewModel: UserViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        NavBar(
            currentRoute = Screens.Map.route,
            onItemClick = { route -> navController.navigate(route) },
            userViewModel = userViewModel,
            content = {
                MapScreenContent()
            })
    }
}

@Composable
fun MapScreenContent() {
    var locationOn by remember { mutableStateOf(true) }
    LocationPermissionManager(
        onPermissionDenied = {
            locationOn = false
        },
        onLocationServiceDisabled = {
            locationOn = false
        }
    ) {
        Map()
    }
    if (!locationOn) {
        LocationErrorScreen()
    }
}