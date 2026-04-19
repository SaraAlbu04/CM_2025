package com.example.ghostmappers.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ghostmappers.R
import com.example.ghostmappers.ui.composables.AchievementsCard
import com.example.ghostmappers.ui.composables.NavBar
import com.example.ghostmappers.ui.composables.PreferencesCard
import com.example.ghostmappers.ui.composables.StatCard
import com.example.ghostmappers.ui.composables.UserProfileCard
import com.example.ghostmappers.ui.navigation.Screens
import com.example.ghostmappers.ui.theme.Maron
import com.example.ghostmappers.ui.views.UserViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    onLogOut: () -> Unit
) {
    NavBar(
        currentRoute = Screens.Profile.route,
        onItemClick = { route -> navController.navigate(route) },
        userViewModel = userViewModel
    ) { paddingValues ->
        ProfileScreenContent(paddingValues, userViewModel, onLogOut)
    }
}

data class PreferenceItem(
    val imageRes: Int,
    val label: String,
    val updateValue: (Boolean) -> Unit,
    val startingState: Boolean
)

@Composable
fun ProfileScreenContent(
    paddingValues: PaddingValues,
    userViewModel: UserViewModel,
    onLogOut: () -> Unit
) {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFECD1))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val profile by userViewModel.userProfile

            // User profile
            UserProfileCard(
                username = profile.username,
                email = profile.email,
                userViewModel = userViewModel
            )

            // Stats (row 1)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    imageRes = R.drawable.reports,
                    title = "Reports",
                    value = profile.numReports.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    imageRes = R.drawable.kills,
                    title = "Kills",
                    value = profile.numKills.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Stats (row 2)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val ghostTypeNum = "${profile.ghostTypesSeen.size} / 6"
                var mostVisitedType = profile.ghostTypeCount.maxByOrNull { it.value }?.key
                if (mostVisitedType == null) {
                    mostVisitedType = "None"
                }
                StatCard(
                    imageRes = R.drawable.ghost_type_stats,
                    title = "Ghosts unlocked",
                    value = ghostTypeNum,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    imageRes = R.drawable.star_icon,
                    title = "Favorite Ghost",
                    value = mostVisitedType,
                    modifier = Modifier.weight(1f)
                )
            }

            // Achievements
            AchievementsCard(profile = profile)

            // Settings
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Preferences
            PreferencesCard(
                preferences = listOf(
                    PreferenceItem(R.drawable.ghost, "Ghost Alerts", userViewModel::updateGhostAlerts, profile.ghostAlerts)
                ),
                switchColor = Color(0xFF782A10)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OutlinedButton(
                        onClick = {
                            onLogOut()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Maron
                        ),
                        border = BorderStroke(width = 2.dp, color = Maron)
                    ) {
                        Row(

                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout Icon",
                                tint = Maron,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Log Out",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}