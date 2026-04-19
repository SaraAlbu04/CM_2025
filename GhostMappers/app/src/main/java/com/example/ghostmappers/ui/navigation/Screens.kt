package com.example.ghostmappers.ui.navigation

sealed class Screens(val route: String) {
    object Report : Screens("Report")
    object Map : Screens("Map")
    object Profile : Screens("Profile")

    object Login : Screens("Login")
}