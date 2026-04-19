package com.example.ghostmappers.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ghostmappers.handlers.handleReportGhost
import com.example.ghostmappers.services.CameraPermissionManager
import com.example.ghostmappers.services.LocationErrorScreen
import com.example.ghostmappers.services.LocationPermissionManager
import com.example.ghostmappers.ui.composables.CameraContent
import com.example.ghostmappers.ui.composables.NavBar
import com.example.ghostmappers.ui.composables.Report
import com.example.ghostmappers.ui.composables.ReportCamera
import com.example.ghostmappers.ui.navigation.Screens
import com.example.ghostmappers.ui.theme.Beige
import com.example.ghostmappers.ui.theme.Orange
import com.example.ghostmappers.ui.views.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(navController: NavController, userViewModel: UserViewModel) {
    var isGlobalLoading by remember { mutableStateOf(false) }
    NavBar(
        currentRoute = Screens.Report.route,
        onItemClick = { route -> navController.navigate(route) },
        userViewModel = userViewModel,
        isLoading = isGlobalLoading,
        content = { paddingValues ->
            CameraScreenContent(onLoadingChanged = { isGlobalLoading = it })
        }
    )
}


@Composable
fun CameraScreenContent(onLoadingChanged: (Boolean) -> Unit) {
    var isCameraOpen by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    var showReport by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var locationOn by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(showReport) {
        if (!showReport) {
            showReport = true
        }
    }
    LocationPermissionManager(
        onPermissionDenied = {
            locationOn = false
        },
        onLocationServiceDisabled = {
            locationOn = false
        }) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Beige)
                .verticalScroll(scrollState)
                .padding(bottom = 200.dp, top = 88.dp)
        ) {
            if (isCameraOpen) {
                CameraPermissionManager(
                    onPermissionDenied = { isCameraOpen = false },
                    content = {
                        CameraContent(onPhotoAccepted = { uri ->
                            capturedImageUri = uri
                            isCameraOpen = false
                        })
                    }
                )
            } else {
                if (capturedImageUri == null) {
                    ReportCamera(
                        onClick = { isCameraOpen = true },
                    )
                }
                if (showReport) {
                    Report(
                        imageUri = capturedImageUri,
                        onReportAccepted = { imageUri, temperature, audioUri, location, ghostStatus, ghostType ->
                            scope.launch {
                                onLoadingChanged(true)
                                isLoading = true
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                val result = handleReportGhost(
                                    imageUri!!,
                                    temperature,
                                    audioUri,
                                    location!!,
                                    ghostStatus,
                                    ghostType,
                                    userId
                                )
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Report accepted!", Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to submit report :(",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                                onLoadingChanged(false)
                                isLoading = false
                            }
                            capturedImageUri = null
                            showReport = false
                        },
                        onReportReset = {
                            capturedImageUri = null
                            showReport = false
                        }
                    )

                }

            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Orange)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Submitting report...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    if (!locationOn) {
        LocationErrorScreen()
    }
}

