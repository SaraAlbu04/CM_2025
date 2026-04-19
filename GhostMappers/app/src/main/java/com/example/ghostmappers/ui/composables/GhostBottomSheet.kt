package com.example.ghostmappers.ui.composables

import android.graphics.BitmapFactory
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.ghostmappers.R
import com.example.ghostmappers.data.model.Ghost
import com.example.ghostmappers.handlers.handleChangeGhostState
import com.example.ghostmappers.ui.theme.Beige
import com.example.ghostmappers.ui.theme.Maron
import com.example.ghostmappers.ui.theme.Orange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GhostBottomSheet(
    ghost: Ghost,
    userId: String,
    distanceToGhost: Double,
    onDirectionsClick: () -> Unit,
    onEnterKillMode: () -> Unit
) {

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .background(Beige)
            .padding(top = 20.dp, bottom = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DirectionsButton(onDirectionsClick)
        val isCloseEnough = distanceToGhost <= 200.0
        KillButton(onEnterKillMode, isCloseEnough)
        GhostBottomSheetDetails(userId, ghost)
    }
}


@Composable
fun KillButton(onEnterKillMode: () -> Unit, isCloseEnough : Boolean) {
    Spacer(modifier = Modifier.height(15.dp))

    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if(isCloseEnough) {
            Column(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEnterKillMode ,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Maron,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.location),
                            contentDescription = "Location Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kill Ghost",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { Toast.makeText(context, "Ghost is too far away", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.location),
                            contentDescription = "Location Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kill Ghost",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

        }

    }
}
@Composable
fun DirectionsButton(onDirectionsClick: () -> Unit) {
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
                onClick = { onDirectionsClick() },
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
                        painter = painterResource(R.drawable.location),
                        contentDescription = "Location Icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Directions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GhostBottomSheetDetails(userId: String, ghost: Ghost) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        ExistingReportScreen(
            userId,
            ghost,
            onBack = { /* navigate back */ }
        )
    }
}


@Composable
fun ExistingReportScreen(
    userId: String,
    ghost: Ghost,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.ghost)
    }

    CompositionLocalProvider(LocalContentColor provides Color.Black) {
        Report(
            imageUri = ghost.imageUrl.toUri(),
            initialTemperature = ghost.temperature,
            initialAudioFileUri = ghost.audioUrl.toUri(),
            initialLocation = ghost.location?.let { geoPoint ->
                Location("firebase").apply {
                    latitude = geoPoint.latitude
                    longitude = geoPoint.longitude
                }
            },
            initialGhostStatus = ghost.status,
            initialGhostType = ghost.type,
            onReportAccepted = { _, _, _, _, ghostStatus, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    val result = handleChangeGhostState(userId, ghost.id, ghostStatus)



                    if (result.isSuccess) {
                        // Switch to main thread to update UI
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Ghost Report Edited!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Failed to edit report",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            },
            onReportReset = onBack,

            )
    }
}