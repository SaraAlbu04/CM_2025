package com.example.ghostmappers.ui.composables

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.ghostmappers.ui.theme.Maron
import java.io.File
import java.io.FileOutputStream

@Composable
fun CameraContent(
    onPhotoAccepted: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    val previewView = remember { PreviewView(context) }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    LaunchedEffect(capturedBitmap) {
        if (capturedBitmap == null) {
            startCamera(context, lifecycleOwner, previewView, imageCapture)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        if (capturedBitmap == null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp)
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2 / 3f)
                        .clip(RoundedCornerShape(16.dp))

                )

                // Camera Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(
                            width = 6.dp,
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Translucent center circle
                    Box(
                        modifier = Modifier
                            .size(64.dp) // Inner circle size
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)) // Semi-transparent inner circle
                            .clickable {
                                isUploading = true
                                takePhoto(
                                    context = context,
                                    imageCapture = imageCapture.value,
                                    onImageCaptured = { bitmap ->
                                        capturedBitmap = bitmap
                                    }
                                )
                            }
                    )
                }
            }
        } else {

            AsyncImage(
                model = capturedBitmap,
                contentDescription = "Captured image preview",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = { capturedBitmap = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Maron,
                        contentColor = Color.White
                    )
                ) {
                    Text("Retake")
                }

                Button(
                    onClick = {
                        capturedBitmap?.let { bitmap ->

                            val uri = saveBitmapToCache(context, bitmap)

                            Toast.makeText(context, "Photo accepted!", Toast.LENGTH_SHORT).show()

                            onPhotoAccepted(uri)
                            capturedBitmap = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Maron,
                        contentColor = Color.White
                    )
                ) {
                    Text("Accept")
                }
            }
        }
    }
}


private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    // Create a unique filename based on timestamp
    val filename = "ghost_${System.currentTimeMillis()}.jpg"
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs() // Make sure directory exists

    val file = File(cachePath, filename)
    val stream = FileOutputStream(file)

    // Compress bitmap to file
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    stream.close()

    // Return the Uri to the file
    return Uri.fromFile(file)
}

private fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    imageCapture: MutableState<ImageCapture?>
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        val newImageCapture = ImageCapture.Builder().build()
        imageCapture.value = newImageCapture

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, newImageCapture)
        } catch (exc: Exception) {
            Log.e("Camera", "Use case binding failed", exc)
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onImageCaptured: (Bitmap) -> Unit,
) {
    if (imageCapture == null) {
        Toast.makeText(context, "Camera not ready.", Toast.LENGTH_SHORT).show()
        return
    }

    val executor = ContextCompat.getMainExecutor(context)
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val bitmap = imageProxyToBitmap(image)
            image.close()
            onImageCaptured(bitmap)
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("Camera", "Photo capture failed: ${exception.message}", exception)
        }
    })
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val matrix = Matrix().apply {
        postRotate(image.imageInfo.rotationDegrees.toFloat())
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
