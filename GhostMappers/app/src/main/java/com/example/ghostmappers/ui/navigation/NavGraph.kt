package com.example.ghostmappers.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ghostmappers.ui.screens.CameraScreen
import com.example.ghostmappers.ui.screens.LoginScreen
import com.example.ghostmappers.ui.screens.MapScreen
import com.example.ghostmappers.ui.screens.ProfileScreen
import com.example.ghostmappers.ui.theme.Orange
import com.example.ghostmappers.ui.views.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(navController: NavHostController, startDestination: String, auth: FirebaseAuth) {

    val userViewModel: UserViewModel = viewModel()

    LaunchedEffect(auth.currentUser?.uid) {
        if (auth.currentUser != null) {
            userViewModel.loadUserProfile()
        }
    }

    NavHost(
        navController = navController,
        startDestination
    )
    {
        composable(route = Screens.Map.route) {
            MapScreen(navController = navController, userViewModel = userViewModel)
        }

        composable(route = Screens.Profile.route) {
            var isInitialLoadDone by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                userViewModel.loadUserProfile {
                    isInitialLoadDone = true
                }
            }

            if (isInitialLoadDone) {
                ProfileScreen(navController = navController, userViewModel = userViewModel, onLogOut = {
                    auth.signOut()
                    navController.navigate(Screens.Login.route)
                })
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFECD1)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
            }
        }

        composable(route = Screens.Report.route) {
            CameraScreen(navController = navController, userViewModel = userViewModel)
        }

        composable(route = Screens.Login.route) {
            LoginScreen(auth = auth, onLoginSuccess = {
                userViewModel.loadUserProfile()
                navController.navigate(Screens.Map.route) {
                    popUpTo(Screens.Login.route) { inclusive = true }
                }
            })
        }


    }
}