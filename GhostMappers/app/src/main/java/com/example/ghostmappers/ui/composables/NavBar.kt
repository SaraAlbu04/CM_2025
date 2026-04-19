package com.example.ghostmappers.ui.composables


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.ghostmappers.R
import com.example.ghostmappers.ui.navigation.Screens
import com.example.ghostmappers.ui.views.ProfileImage
import com.example.ghostmappers.ui.views.UserViewModel

val bottomBarItems = listOf(
    Screens.Report.route,
    Screens.Map.route,
    Screens.Profile.route
)

@Composable
fun NavBar(
    currentRoute: String,
    onItemClick: (String) -> Unit,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    val profileImage = userViewModel.profileImage.value
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier,
            containerColor = Color.DarkGray,
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                        .height(88.dp)
                        .clip(RoundedCornerShape(32.dp)),
                    containerColor = Color(0xFF1F1F23),
                    windowInsets = WindowInsets(0.dp)
                ) {
                    bottomBarItems.forEach { screen ->
                        NavigationBarItem(
                            enabled = !isLoading,
                            selected = (currentRoute == screen),
                            onClick = {
                                onItemClick(screen)
                            },
                            label = {
                                Text(screen)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            ),
                            icon = {
                                when (screen) {
                                    Screens.Report.route -> Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = "Camera",
                                        modifier = Modifier.size(30.dp)
                                    )

                                    Screens.Map.route -> Icon(
                                        painter = painterResource(id = R.drawable.map),
                                        contentDescription = "Map",
                                        modifier = Modifier.size(30.dp)
                                    )

                                    Screens.Profile.route -> {
                                        when (profileImage) {
                                            is ProfileImage.Preset -> Image(
                                                painter = painterResource(id = profileImage.resId),
                                                contentDescription = "Profile",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                            )

                                            is ProfileImage.Uploaded -> Image(
                                                painter = rememberAsyncImagePainter(profileImage.url),
                                                contentDescription = "Profile",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            content(paddingValues)
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    },
                contentAlignment = Alignment.Center
            ) { }
        }
    }
}



