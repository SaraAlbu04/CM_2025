package com.example.ghostmappers.ui.composables

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ghostmappers.R
import com.example.ghostmappers.services.LocationService
import com.example.ghostmappers.ui.theme.Beige
import com.example.ghostmappers.ui.theme.LightBlue
import com.example.ghostmappers.ui.theme.Maron
import kotlinx.coroutines.launch

@Composable
fun Location(location: Location?, onLocationChange: (Location) -> Unit) {
    LocationContent(location, onLocationChange)
}

@Composable
private fun LocationContent(location: Location?, onLocationChange: (Location) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationService = remember { LocationService(context) }
    var locationText: String? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(false) }



    if (location != null) {
        LaunchedEffect(location) {
            locationText = locationService.getAddressFromLocation(location)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Beige)
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(color = Color.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.location),
                        contentDescription = "Location",
                        modifier = Modifier.size(24.dp),
                        tint = LightBlue,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("Location", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            locationText?.let {
                Text(
                    it,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(top = 4.dp, start = 36.dp)
                )
            }
        }

    } else {
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
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.location),
                    contentDescription = "Location",
                    modifier = Modifier.size(24.dp),
                    tint = LightBlue,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text("Location", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val locationCoords = locationService.getCurrentLocation()

                            if (locationCoords != null) {
                                locationText =
                                    locationService.getAddressFromLocation(locationCoords)
                                onLocationChange(locationCoords)
                            }
                            isLoading = false
                        } catch (e: Exception) {
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Maron,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(90.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Get")
                }
            }
        }
    }

}