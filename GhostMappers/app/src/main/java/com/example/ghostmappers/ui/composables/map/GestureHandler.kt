package com.example.ghostmappers.ui.composables.map

import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.android.gestures.RotateGestureDetector
import com.mapbox.android.gestures.ShoveGestureDetector
import com.mapbox.android.gestures.StandardScaleGestureDetector
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.OnRotateListener
import com.mapbox.maps.plugin.gestures.OnScaleListener
import com.mapbox.maps.plugin.gestures.OnShoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.navigation.ui.maps.camera.NavigationCamera

fun setupGestureListeners(
    mapView: MapView,
    navigationCamera: NavigationCamera?,
    onUserControlDetected: () -> Unit
) {
    // Pan/Drag gesture
    mapView.gestures.addOnMoveListener(object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onUserControlDetected()
            navigationCamera?.requestNavigationCameraToIdle()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean = false
        override fun onMoveEnd(detector: MoveGestureDetector) {}
    })

    // Rotation gesture
    mapView.gestures.addOnRotateListener(object : OnRotateListener {
        override fun onRotateBegin(detector: RotateGestureDetector) {
            onUserControlDetected()
            navigationCamera?.requestNavigationCameraToIdle()
        }

        override fun onRotate(detector: RotateGestureDetector) {}
        override fun onRotateEnd(detector: RotateGestureDetector) {}
    })

    // Zoom gesture
    mapView.gestures.addOnScaleListener(object : OnScaleListener {
        override fun onScaleBegin(detector: StandardScaleGestureDetector) {
            onUserControlDetected()
            navigationCamera?.requestNavigationCameraToIdle()
        }

        override fun onScale(detector: StandardScaleGestureDetector) {}
        override fun onScaleEnd(detector: StandardScaleGestureDetector) {}
    })

    // Pitch gesture
    mapView.gestures.addOnShoveListener(object : OnShoveListener {
        override fun onShoveBegin(detector: ShoveGestureDetector) {
            onUserControlDetected()
            navigationCamera?.requestNavigationCameraToIdle()
        }

        override fun onShove(detector: ShoveGestureDetector) {}
        override fun onShoveEnd(detector: ShoveGestureDetector) {}
    })

    // Fling gesture
    mapView.gestures.addOnFlingListener {
        onUserControlDetected()
        navigationCamera?.requestNavigationCameraToIdle()
    }
}