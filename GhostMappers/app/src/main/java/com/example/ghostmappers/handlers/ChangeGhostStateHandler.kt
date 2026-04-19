package com.example.ghostmappers.handlers

import android.util.Log
import com.example.ghostmappers.data.GhostRepository

suspend fun handleChangeGhostState(
    editorId: String,
    ghostId: String,
    newStatus: String
): Result<Boolean> {
    return try {
        val ghostRepository = GhostRepository()
        ghostRepository.updateGhostStatus(editorId, ghostId, newStatus)
        Result.success(true)
    } catch (e: Exception) {
        Log.e("GhostRepository", "Error changing ghost state", e)
        Result.failure(e)
    }
}