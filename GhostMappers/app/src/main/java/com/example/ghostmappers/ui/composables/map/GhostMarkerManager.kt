package com.example.ghostmappers.ui.composables.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.graphics.createBitmap
import com.example.ghostmappers.R
import com.example.ghostmappers.data.model.Ghost
import com.google.gson.JsonObject
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location

class GhostMarkerManager(private val mapView: MapView) {

    private val markerManager = mapView.annotations.createPointAnnotationManager()

    fun updateMarkers(ghosts: List<Ghost>, onGhostClick: (String) -> Unit) {
        markerManager.deleteAll()

        markerManager.addClickListener { annotation ->
            val clickedGhostId = annotation.getData()?.asJsonObject?.get("id")?.asString
            if (clickedGhostId != null) {
                onGhostClick(clickedGhostId)
            }
            true
        }

        ghosts.forEach { ghost ->
            addGhostMarker(ghost)
        }

        // Refresh location
        mapView.location.enabled = false
        mapView.location.enabled = true
    }

    private fun addGhostMarker(ghost: Ghost) {
        Log.d("Map", "Processing ghost: $ghost")

        val drawableRes = getGhostDrawable(ghost.type)
        var bitmap = BitmapFactory.decodeResource(mapView.context.resources, drawableRes)

        if (bitmap == null) {
            Log.e("Map", "Failed to decode PNG for ghost type: ${ghost.type}")
            return
        }

        bitmap = when (ghost.status) {
            "Dead" -> applyColorFilter(bitmap, FilterType.GRAYSCALE)
            "Mad" -> applyColorFilter(bitmap, FilterType.RED_HUE)
            else -> bitmap // Normal color
        }

        ghost.location?.let { firebaseLocation ->
            val mapboxPoint = Point.fromLngLat(
                firebaseLocation.longitude,
                firebaseLocation.latitude
            )

            val dataObject = JsonObject().apply {
                addProperty("id", ghost.id)
            }

            val options = PointAnnotationOptions()
                .withPoint(mapboxPoint)
                .withIconImage(bitmap)
                .withIconSize(0.5)
                .withData(dataObject)

            markerManager.create(options)
        }
    }

    private fun getGhostDrawable(type: String): Int = when (type) {
        "Normal" -> R.drawable.ghost_default
        "Cat" -> R.drawable.ghost_cat
        "Witch" -> R.drawable.ghost_witch
        "Dog" -> R.drawable.ghost_dog
        "Drunk" -> R.drawable.ghost_drunk
        "Reader" -> R.drawable.ghost_reader
        else -> R.drawable.ghost_default
    }

    enum class FilterType { GRAYSCALE, RED_HUE }

    private fun applyColorFilter(sourceBitmap: Bitmap, type: FilterType): Bitmap {
        val resultBitmap = createBitmap(sourceBitmap.width, sourceBitmap.height)
        val canvas = android.graphics.Canvas(resultBitmap)
        val paint = android.graphics.Paint()
        val matrix = android.graphics.ColorMatrix()

        when (type) {
            FilterType.GRAYSCALE -> {
                matrix.setSaturation(0f)
            }
            FilterType.RED_HUE -> {
                // Matrix structure: [ R, G, B, A, Offset ]
                matrix.set(floatArrayOf(
                    1.5f, 0f, 0f, 0f, 50f, // Red: boost intensity and add offset
                    0f, 0.5f, 0f, 0f, 0f,  // Green: reduce
                    0f, 0f, 0.5f, 0f, 0f,  // Blue: reduce
                    0f, 0f, 0f, 1f, 0f     // Alpha: keep same
                ))
            }
        }

        paint.colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(sourceBitmap, 0f, 0f, paint)
        return resultBitmap
    }
}