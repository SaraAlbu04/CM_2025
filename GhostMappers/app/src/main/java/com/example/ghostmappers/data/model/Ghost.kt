package com.example.ghostmappers.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

data class Ghost(
    @DocumentId
    val id: String = "",
    val imageUrl: String = "",
    val temperature: Double? = null,
    val audioUrl: String = "",
    val location: GeoPoint? = null,
    val status: String = "",
    val type: String = "",
    val reporterId: String = "",
    @ServerTimestamp
    val created: Timestamp? = null,
)