package com.example.ghostmappers.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationService(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)


    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            continuation.resumeWithException(SecurityException("Location permission not granted"))
            return@suspendCancellableCoroutine
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location: Location? ->
            continuation.resume(location)
        }.addOnFailureListener { e ->
            continuation.resumeWithException(e)
        }
    }


    suspend fun getAddressFromLocation(location: Location): String? = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.getDefault())

        if (!Geocoder.isPresent()) return@withContext null


        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                val address = addresses.firstOrNull()
                continuation.resume(formatAddress(address))
            }
        }
    }

    private fun formatAddress(address: Address?): String? {
        return address?.getAddressLine(0)
    }
}