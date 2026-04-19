package com.example.ghostmappers.handlers

import android.location.Location
import android.net.Uri
import android.util.Log
import com.example.ghostmappers.data.GhostRepository
import com.example.ghostmappers.data.UserRepository
import com.example.ghostmappers.data.model.Ghost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint

suspend fun handleReportGhost(
    imageUri: Uri,
    temperature: Double?,
    audioUri: Uri?,
    location: Location,
    ghostStatus: String,
    type: String,
    reporterId: String?
): Result<Boolean> {
    return try {
        val ghostRepository = GhostRepository()
        val userRepository = UserRepository()

        val newGhost = Ghost(
            temperature = temperature,
            location = GeoPoint(
                location.latitude,
                location.longitude
            ),
            status = if (ghostStatus != "") ghostStatus else "Peaceful",
            type = type,
            reporterId = reporterId ?: "anonymous"

        )

        ghostRepository.addGhostReport(
            reporterId = FirebaseAuth.getInstance().currentUser?.uid
                ?: "anonymous",
            ghost = newGhost,
            imageUri = imageUri,
            audioUri = audioUri
        )
        if (reporterId != null) {
            userRepository.incrementReportCount(reporterId)
            userRepository.updateGhostsTypeSeen(reporterId, type)
            userRepository.incrementGhostTypeCount(reporterId, type)
        }
        Result.success(true)
    } catch (e: Exception) {
        Log.e("GhostRepository", "Error reporting ghost", e)
        Result.failure(e)

    }
}