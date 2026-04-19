package com.example.ghostmappers.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.ghostmappers.R
import com.example.ghostmappers.data.GhostRepository
import com.example.ghostmappers.data.model.Ghost
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GhostForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var locationService: LocationService
    private val ghostRepository = GhostRepository()

    private val notifiedGhosts = mutableSetOf<String>()

    companion object {
        const val SERVICE_CHANNEL_ID = "ghost_service_channel"
        const val GHOST_CHANNEL_ID = "ghost_channel"
        const val SERVICE_NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        locationService = LocationService(this)
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            SERVICE_NOTIFICATION_ID,
            buildServiceNotification()
        )

        serviceScope.launch {
            while (isActive) {
                checkNearbyGhosts()
                delay(5_000) // every 5 seconds
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun getGhostAlertsPreference(): Boolean {
        val myUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return false

        return try {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(myUserId)
                .get()
                .await()
            doc.getBoolean("ghostAlerts") ?: false
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun checkNearbyGhosts() {
        val myUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Check if ghost alerts are enabled in user preferences
        val ghostAlertsEnabled = getGhostAlertsPreference()

        if (!ghostAlertsEnabled) return

        val location = try {
            locationService.getCurrentLocation()
        } catch (e: Exception) {
            null
        } ?: return

        val oneHourAgo = Timestamp.now().seconds - 3600

        val userPoint = Point.fromLngLat(location.longitude, location.latitude)
        val ghosts = ghostRepository.getAllGhosts()

        ghosts.forEach { ghost ->
            val ghostLocation = ghost.location ?: return@forEach

            if (
                ghost.reporterId == myUserId &&
                ghost.created?.seconds!! > oneHourAgo
            ) return@forEach

            if (ghost.id in notifiedGhosts) return@forEach

            val ghostPoint = Point.fromLngLat(
                ghostLocation.longitude,
                ghostLocation.latitude
            )

            val distance = TurfMeasurement.distance(
                userPoint,
                ghostPoint,
                TurfConstants.UNIT_METERS
            )

            if (distance <= 50.0 && ghost.status != "Dead") {
                notifyGhostNearby(ghost)
                notifiedGhosts.add(ghost.id)
            }
        }
    }


    private fun notifyGhostNearby(ghost: Ghost) {

        val notification = NotificationCompat.Builder(this, GHOST_CHANNEL_ID)
            .setSmallIcon(R.drawable.ghost)
            .setContentTitle("👻 Ghost nearby!")
            .setContentText("A ${ghost.type} ghost is close to you.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ghost.id.hashCode(), notification)
    }

    private fun buildServiceNotification(): Notification {
        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ghost)
            .setContentTitle("Ghost Mapper Active")
            .setContentText("Tracking nearby ghosts…")
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            "Ghost Tracking Service",
            NotificationManager.IMPORTANCE_LOW
        )

        val soundUri = "android.resource://${packageName}/raw/ghost_sound".toUri()

        val ghostChannel = NotificationChannel(
            GHOST_CHANNEL_ID,
            "Ghost Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(
                soundUri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }

        manager.createNotificationChannel(serviceChannel)
        manager.createNotificationChannel(ghostChannel)
    }
}