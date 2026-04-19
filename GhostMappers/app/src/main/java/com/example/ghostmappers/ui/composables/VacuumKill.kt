package com.example.ghostmappers.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ghostmappers.R
import com.example.ghostmappers.ui.theme.Orange
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun VacuumKill(
    onKillConfirmed: () -> Unit,
    onCancel: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, _ -> change.consume() }
            }
    ) {
        val density = LocalDensity.current
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        val dropZoneRadiusPx = with(density) { 100.dp.toPx() }

        val isInsideZone = remember(offsetX, offsetY) {
            val centerX = with(density) { screenWidth.toPx() / 2 }
            val centerY = with(density) { screenHeight.toPx() / 2 }
            val initialVacX = with(density) { screenWidth.toPx() / 2 }
            val initialVacY = with(density) { 100.dp.toPx() }

            val currentVacX = initialVacX + offsetX
            val currentVacY = initialVacY + offsetY

            val distance = hypot(
                (centerX - currentVacX).toDouble(),
                (centerY - currentVacY).toDouble()
            )
            distance < dropZoneRadiusPx
        }

        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen)
        ) {
            drawRect(color = Color.Black.copy(alpha = 0.4f))

            drawCircle(
                color = Color.Transparent,
                radius = dropZoneRadiusPx,
                center = center,
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )
        }

        val targetColor = if (isInsideZone) Color.Green else Color.Red

        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isInsideZone) "RELEASE TO KILL" else "VACUUM ZONE",
                color = targetColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.offset(y = (-120).dp)
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(2.dp, targetColor, CircleShape)
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = Color.White
                )
            ) {
                Text("Cancel")
            }

            Icon(
                painter = painterResource(R.drawable.vacuum),
                contentDescription = "Vacuum",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                val centerX = with(density) { screenWidth.toPx() / 2 }
                                val centerY = with(density) { screenHeight.toPx() / 2 }

                                val initialVacX = with(density) { screenWidth.toPx() / 2 }
                                val initialVacY = with(density) { 100.dp.toPx() }

                                val currentVacX = initialVacX + offsetX
                                val currentVacY = initialVacY + offsetY

                                val distance = hypot(
                                    (centerX - currentVacX).toDouble(),
                                    (centerY - currentVacY).toDouble()
                                )

                                if (distance < dropZoneRadiusPx) {
                                    onKillConfirmed()
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
            )
        }
    }
}