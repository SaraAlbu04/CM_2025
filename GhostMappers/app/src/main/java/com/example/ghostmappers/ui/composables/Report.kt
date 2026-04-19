package com.example.ghostmappers.ui.composables

import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ghostmappers.R
import com.example.ghostmappers.ui.theme.Beige
import com.example.ghostmappers.ui.theme.LightBlue
import com.example.ghostmappers.ui.theme.Maron
import com.example.ghostmappers.ui.theme.Orange
import com.example.ghostmappers.utils.Constants

@Composable
fun Report(
    imageUri: Uri?,
    initialTemperature: Double? = null,
    initialAudioFileUri: Uri? = null,
    initialLocation: Location? = null,
    initialGhostStatus: String = "",
    initialGhostType: String = "",
    onReportAccepted: (image: Uri?, temperature: Double?, audioFileUri: Uri?, location: Location?, ghostStatus: String, ghostType: String) -> Unit,
    onReportReset: () -> Unit
) {
    val context = LocalContext.current

    var temperature by remember { mutableStateOf(initialTemperature) }
    var audioFileUri by remember { mutableStateOf(initialAudioFileUri) }
    var location by remember { mutableStateOf(initialLocation) }
    var ghostStatus by remember { mutableStateOf(initialGhostStatus) }
    var ghostType by remember { mutableStateOf(initialGhostType) }

    if (imageUri != null)
        GhostImageCard(image = imageUri)
    Spacer(modifier = Modifier.height(16.dp))
    CharacteristicsCard(
        temperature = temperature,
        onTemperatureChange = { newTemp ->
            temperature = newTemp
        },
        audioFileUri = audioFileUri,
        onAudioFileUriChange = { newUri ->
            audioFileUri = newUri
        },
        onRecordingDelete = {
            audioFileUri = null
        },
        location = location,
        onLocationChange = { newLocation ->
            location = newLocation
        },
        ghostStatus = ghostStatus,
        onGhostStatusSelected = { ghostStatus = it },
        ghostType = ghostType,
        onGhostTypeSelected = { ghostType = it },
    )
    Spacer(modifier = Modifier.height(16.dp))
    ReportActionsCard(
        onReportAccepted = {
            if (imageUri == null) {
                Toast.makeText(context, "Image is required", Toast.LENGTH_SHORT).show()
            } else if (location == null) {
                Toast.makeText(context, "Location is required", Toast.LENGTH_SHORT).show()
            } else if (ghostType.isEmpty()) {
                Toast.makeText(context, "Ghost Type is required", Toast.LENGTH_SHORT).show()
            } else {
                onReportAccepted(
                    imageUri,
                    temperature,
                    audioFileUri,
                    location,
                    ghostStatus,
                    ghostType
                )
            }
        },
        onReportReset = onReportReset
    )

}

@Composable
private fun GhostImageCard(image: Uri) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AsyncImage(
            model = image,
            contentDescription = "Captured image preview",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
    }
}

@Composable
private fun CharacteristicsCard(
    temperature: Double?,
    onTemperatureChange: (Double) -> Unit,
    audioFileUri: Uri?,
    onAudioFileUriChange: (Uri) -> Unit,
    onRecordingDelete: () -> Unit,
    location: Location?,
    onLocationChange: (Location) -> Unit,
    ghostStatus: String,
    onGhostStatusSelected: (String) -> Unit,
    ghostType: String,
    onGhostTypeSelected: (String) -> Unit,
) {
    val ghostStatusItems = Constants.GHOST_STATUS_ITEMS
    val ghostTypeItems = Constants.GHOST_TYPE_ITEMS

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 24.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Characteristics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Temperature(temperature, onTemperatureChange)
            VoiceRecorder(audioFileUri, onAudioFileUriChange, onRecordingDelete)
            Location(location, onLocationChange)
            GhostCharacteristic(
                icon = R.drawable.heartbeat,
                label = "Ghost Status",
                items = ghostStatusItems,
                selectedItem = ghostStatus,
                onItemSelected = onGhostStatusSelected
            )
            GhostCharacteristic(
                icon = R.drawable.category,
                label = "Ghost Type",
                items = ghostTypeItems,
                selectedItem = ghostType,
                onItemSelected = onGhostTypeSelected


            )
        }
    }
}


@Composable
fun CharacteristicAction(
    label: String,
    buttonText: String,
    onClick: () -> Unit,
    icon: Int,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Beige)
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(color = Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center,

            ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = "Action Icon",
                modifier = Modifier.size(24.dp),
                tint = LightBlue,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.weight(1f))
        var buttonContainerColor = Maron
        if (!enabled) {
            buttonContainerColor = Maron.copy(alpha = 0.5f)
        }

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonContainerColor,
                contentColor = Color.White
            ),

            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(90.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) { Text(buttonText) }
    }
}


@Composable
private fun ReportActionsCard(onReportAccepted: () -> Unit, onReportReset: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    onReportAccepted()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.upload),
                        contentDescription = "Upload Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send Report",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Report reset!", Toast.LENGTH_SHORT).show()
                    onReportReset()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = LightBlue
                ),
                border = BorderStroke(width = 2.dp, color = LightBlue)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Replay Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Report",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


