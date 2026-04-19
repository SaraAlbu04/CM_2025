package com.example.ghostmappers.services

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

@Composable
fun LocationPermissionManager(
    onPermissionDenied: () -> Unit,
    onLocationServiceDisabled: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    var arePermissionsGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isLocationEnabled by remember { mutableStateOf(false) }

    val locationSettingLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isLocationEnabled = true
        } else {
            onLocationServiceDisabled()
        }
    }

    // Helper function
    fun checkAndRequestLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            isLocationEnabled = true
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    locationSettingLauncher.launch(intentSenderRequest)
                } catch (e: IntentSender.SendIntentException) {
                    // Ignore error
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fine && coarse) {
            arePermissionsGranted = true
        } else {
            onPermissionDenied()
        }
    }


    LaunchedEffect(arePermissionsGranted) {
        if (arePermissionsGranted) {
            // If permissions are good, proceed to check GPS
            checkAndRequestLocation()
        } else {
            // If permissions are missing, ask for them
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    if (arePermissionsGranted && isLocationEnabled) {
        content()

    }
}


@Composable
fun LocationErrorScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Location permission denied or GPS is disabled.",
            modifier = Modifier.padding(horizontal = 20.dp)
        )

    }
}