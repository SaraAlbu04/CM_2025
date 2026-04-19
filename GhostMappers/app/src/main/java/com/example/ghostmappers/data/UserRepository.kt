package com.example.ghostmappers.data

import android.util.Log
import com.example.ghostmappers.utils.Constants.GHOST_TYPE_ITEMS
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = Firebase.firestore


    suspend fun updateGhostsTypeSeen(
        uid: String,
        ghostType: String
    ): Result<Boolean> {
        return try {
            val snapshot = db.collection("users").document(uid)
            val allGhostTypes = GHOST_TYPE_ITEMS
            if (ghostType in allGhostTypes) {
                snapshot.update("ghostTypesSeen", FieldValue.arrayUnion(ghostType)).await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating ghosts type seen", e)
            Result.failure(e)

        }

    }

    suspend fun incrementReportCount(
        uid: String
    ): Result<Boolean> {
        return try {
            db.collection("users").document(uid).update("numReports", FieldValue.increment(1))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error incrementing report count", e)
            Result.failure(e)
        }

    }

    suspend fun incrementKillCount(
        uid: String
    ): Result<Boolean> {
        return try {
            db.collection("users").document(uid).update("numKills", FieldValue.increment(1))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error incrementing kill count", e)
            Result.failure(e)
        }

    }

    suspend fun incrementGhostTypeCount(
        uid: String,
        ghostType: String
    ): Result<Boolean> {
        return try {
            val ghostTypeKey = "ghostTypeCount.${ghostType.lowercase()}"
            db.collection("users").document(uid).update(ghostTypeKey, FieldValue.increment(1))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error incrementing ghost type count", e)
            Result.failure(e)
        }
    }


    fun createNewAccount(
        uid: String,
        username: String,
        email: String
    ): Result<Boolean> {
        return try {
            val ghostTypesSeen: List<String> = emptyList()
            val ghostTypeCount: Map<String, Long> = emptyMap()
            val userData = mapOf(
                "username" to username,
                "email" to email,
                "numReports" to 0,
                "numKills" to 0,
                "ghostTypesSeen" to ghostTypesSeen,
                "ghostTypeCount" to ghostTypeCount
            )
            db.collection("users").document(uid).set(userData)
            Result.success(true)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error creating new account", e)
            Result.failure(e)
        }
    }



}