package com.example.ghostmappers.data

import android.net.Uri
import android.util.Log
import com.example.ghostmappers.data.model.Ghost
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GhostRepository {
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    fun getGhostsFlow(): Flow<List<Ghost>> = callbackFlow {
        val subscription = db.collection("ghosts")
            .orderBy("created", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("GhostRepository", "Listener in the error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val allGhosts = snapshot.toObjects(Ghost::class.java)
                    val aliveGhosts = allGhosts.filter { it.status != "Dead" }
                    trySend(aliveGhosts)
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun getAllGhosts(): List<Ghost> {
        return try {
            val snapshot = db.collection("ghosts")
                .orderBy("created", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d("GhostRepository", "Found ${snapshot.documents.size} documents.")
            for (document in snapshot.documents) {
                Log.d("GhostRepository", "Document ID: ${document.id}")
                Log.d(
                    "GhostRepository",
                    "Document Data: ${document.data}"
                )
            }
            snapshot.toObjects(Ghost::class.java)
        } catch (e: Exception) {
            Log.e("GhostRepository", "Error fetching ghosts", e)
            emptyList()
        }
    }

    suspend fun addGhostReport(
        reporterId: String,
        ghost: Ghost,
        imageUri: Uri,
        audioUri: Uri?
    ): Result<Boolean> {
        return try {
            coroutineScope {
                // Upload image and audio files in parallel
                val imageUrlDeferred = async {
                    uploadFileAndGetUrl(
                        storage.reference.child("images/${UUID.randomUUID()}"),
                        imageUri
                    )
                }

                // Only upload audio if the URI is not null
                val audioUrlDeferred = audioUri?.let { uri ->
                    async {
                        uploadFileAndGetUrl(
                            storage.reference.child("audio/${UUID.randomUUID()}"),
                            uri
                        )
                    }
                }

                val imageUrl = imageUrlDeferred.await()
                val audioUrl = audioUrlDeferred?.await()

                val finalGhost = ghost.copy(
                    reporterId = reporterId,
                    imageUrl = imageUrl,
                    audioUrl = audioUrl ?: ""
                )

                db.collection("ghosts").add(finalGhost).await()

                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e("GhostRepository", "Error adding ghost report", e)
            Result.failure(e)
        }
    }

    suspend fun updateGhostStatus(
        editorId: String,
        ghostId: String,
        newStatus: String
    ): Result<Boolean> {
        return try {
            db.collection("ghosts").document(ghostId)
                .update(
                    "status", newStatus,
                    "lastEditorId", editorId
                )
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("GhostRepository", "Error updating ghost status", e)
            Result.failure(e)
        }
    }

    private suspend fun uploadFileAndGetUrl(storageRef: StorageReference, uri: Uri): String {
        storageRef.putFile(uri).await() // Upload the file
        return storageRef.downloadUrl.await().toString() // Get the download URL
    }
}