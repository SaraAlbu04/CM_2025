package com.example.ghostmappers.handlers

import android.util.Log
import com.example.ghostmappers.data.GhostRepository
import com.example.ghostmappers.data.UserRepository

suspend fun handleKillGhost(
    editorId: String,
    ghostId: String,
): Result<Boolean> {
    return try {
        val ghostRepository = GhostRepository()
        val userRepository = UserRepository()
        ghostRepository.updateGhostStatus(editorId, ghostId, "Dead")
        userRepository.incrementKillCount(editorId)
        Result.success(true)
    } catch (e: Exception) {
        Log.e("GhostRepository", "Error killing ghost", e)
        Result.failure(e)
    }
}