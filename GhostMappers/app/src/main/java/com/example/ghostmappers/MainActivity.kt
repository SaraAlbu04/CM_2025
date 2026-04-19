package com.example.ghostmappers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.ghostmappers.ui.navigation.NavGraph
import com.example.ghostmappers.ui.navigation.Screens
import com.example.ghostmappers.ui.theme.GhostMappersTheme
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GhostMappersTheme {
                val navController = rememberNavController()
                AppEntryPoint(navController)

            }
        }
    }
}


@Composable
fun AppEntryPoint(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) Screens.Map.route else Screens.Login.route

    NavGraph(
        navController = navController,
        startDestination = startDestination,
        auth = auth,
    )

}



