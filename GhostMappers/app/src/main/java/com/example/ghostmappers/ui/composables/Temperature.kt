package com.example.ghostmappers.ui.composables


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ghostmappers.R
import com.example.ghostmappers.services.WeatherService
import com.example.ghostmappers.ui.theme.Beige
import com.example.ghostmappers.ui.theme.LightBlue
import com.example.ghostmappers.ui.theme.Maron
import kotlinx.coroutines.launch

@Composable
fun Temperature(temperature: Double?, onTemperatureChange: (Double) -> Unit) {
    LocalContext.current
    var showExistingTemperature by remember { mutableStateOf(true) }

    if (temperature != null && showExistingTemperature) {
        ExistingTemperature(temperature)
    } else {
        TemperatureContent(
            temperature = temperature,
            onTemperatureChange = onTemperatureChange,
            onDone = {
                showExistingTemperature = false
            }
        )
    }

}

@Composable
private fun TemperatureContent(
    temperature: Double?,
    onTemperatureChange: (Double) -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    var temperatureText by remember(temperature) { mutableStateOf(temperature?.toString() ?: "") }
    val weatherService = WeatherService(context)
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var isTemperatureSet by remember { mutableStateOf(temperature != null) }

    val focusManager = LocalFocusManager.current



    if (!isTemperatureSet) {
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
                    painter = painterResource(R.drawable.thermometer),
                    contentDescription = "Temperature",
                    modifier = Modifier.size(24.dp),
                    tint = LightBlue,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text("Temperature", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val fetchedTemp = weatherService.getCurrentTemperature()
                            onTemperatureChange(fetchedTemp)
                            onDone()
                            isTemperatureSet = true
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Failed to get current temperature.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        isLoading = false


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
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Beige)
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(color = Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.thermometer),
                    contentDescription = "Temperature",
                    modifier = Modifier.size(24.dp),
                    tint = LightBlue,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text("Temperature", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(40.dp)
                    .border(
                        width = 1.dp,
                        color = Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = temperatureText,
                    onValueChange = { newValue ->
                        if (newValue.toDoubleOrNull() != null) {
                            temperatureText = newValue
                            onTemperatureChange(newValue.toDouble())
                        }


                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions { focusManager.clearFocus() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "°C", fontSize = 16.sp)

        }

    }
}

@Composable
private fun ExistingTemperature(temperature: Double?) {
    var temperatureText by remember(temperature) { mutableStateOf(temperature?.toString() ?: "") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Beige)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(color = Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.thermometer),
                contentDescription = "Temperature",
                modifier = Modifier.size(24.dp),
                tint = LightBlue,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("Temperature", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))

        Text(text = temperatureText)

        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "°C", fontSize = 16.sp)

    }
}