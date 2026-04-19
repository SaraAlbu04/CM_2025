package com.example.ghostmappers.ui.composables

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ghostmappers.R
import com.example.ghostmappers.ui.screens.PreferenceItem
import com.example.ghostmappers.ui.theme.Maron
import com.example.ghostmappers.ui.views.ProfileImage
import com.example.ghostmappers.ui.views.UserProfile
import com.example.ghostmappers.ui.views.UserViewModel

@Composable
fun UserProfileCard(
    username: String,
    email: String,
    userViewModel: UserViewModel
) {
    LaunchedEffect(Unit) {
        userViewModel.loadUserProfile()
    }

    val halloweenImages = listOf(
        R.drawable.halloween_pumpkin,
        R.drawable.halloween_ghost,
        R.drawable.halloween_bat,
        R.drawable.halloween_cat,
        R.drawable.halloween_skull
    )

    var menuExpanded by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Upload the selected image
            userViewModel.uploadProfileImage(it)
        }
    }

    val painter = rememberAsyncImagePainter(
        when (val image = userViewModel.profileImage.value) {
            is ProfileImage.Uploaded -> image.url
            is ProfileImage.Preset -> image.resId
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    painter = painter,
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .fillMaxSize()
                )

                Card(
                    modifier = Modifier.size(30.dp),
                    onClick = { menuExpanded = true }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.camera_shuffle),
                        contentDescription = "Change photo",
                        modifier = Modifier.padding(4.dp)
                    )
                }

                // Dropdown menu for choices
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(Color.White),
                    offset = DpOffset(x = 0.dp, y = 4.dp)
                )  {
                    DropdownMenuItem(
                        text = { Text("Upload Image") },
                        onClick = {
                            launcher.launch("image/*")
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Shuffle Preset") },
                        onClick = {
                            val newPreset = halloweenImages.random()
                            userViewModel.selectPreset(newPreset)
                            menuExpanded = false
                        }
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(username, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(email, fontSize = 16.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun StatCard(
    imageRes: Int,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(2.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun AchievementsCard(profile: UserProfile) {
    val achievements = listOf(
        AchievementData(
            title = "Top Reporter",
            description = "Submitted 10+ reports",
            iconRes = R.drawable.reports_achieve,
            isUnlocked = profile.numReports >= 10
        ),
        AchievementData(
            title = "Ghost Hunter",
            description = "Killed 5+ ghosts",
            iconRes = R.drawable.hunter_achieve,
            isUnlocked = profile.numKills >= 5
        ),
        AchievementData(
            title = "Master Collector",
            description = "Unlocked all 6 ghost types",
            iconRes = R.drawable.collection_achieve,
            isUnlocked = profile.ghostTypesSeen.size >= 6
        )
    )

    val unlockedAchievements = achievements.filter { it.isUnlocked }

    if (unlockedAchievements.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Achievements",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                unlockedAchievements.forEach { achievement ->
                    AchievementRow(achievement)
                }
            }
        }
    }
}

data class AchievementData(
    val title: String,
    val description: String,
    val iconRes: Int,
    val isUnlocked: Boolean
)

@Composable
private fun AchievementRow(achievement: AchievementData) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = achievement.iconRes),
            contentDescription = achievement.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFEFEF))
                .padding(6.dp)
        )

        Column {
            Text(
                text = achievement.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = achievement.description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PreferencesCard(
    preferences: List<PreferenceItem>, // a list of preferences
    switchColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Preferences",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )


            preferences.forEach { pref ->
                PreferenceRow(
                    imageRes = pref.imageRes,
                    label = pref.label,
                    switchColor = switchColor,
                    updateValue = pref.updateValue,
                    startingState = pref.startingState
                )
            }
        }
    }
}

@Composable
private fun PreferenceRow(
    imageRes: Int,
    label: String,
    switchColor: Color,
    updateValue: (Boolean) -> Unit,
    startingState: Boolean
) {
    var isEnabled by remember { mutableStateOf(startingState) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side (icon + text)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = imageRes),
                        contentDescription = label,
                        modifier = Modifier.size(28.dp),
                        tint = Maron
                    )
                }
            }

            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        // Right side (toggle)
        Switch(
            checked = isEnabled,
            onCheckedChange = {
                isEnabled = it
                updateValue(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = switchColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray
            )
        )
    }
}