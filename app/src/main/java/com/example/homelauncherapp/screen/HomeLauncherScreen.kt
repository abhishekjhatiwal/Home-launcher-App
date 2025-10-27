package com.example.homelauncherapp.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.homelauncherapp.R
import com.example.homelauncherapp.data.AppInfo
import com.example.homelauncherapp.getInstalledApps
import com.example.homelauncherapp.launchApp

@Composable
fun HomeLauncherScreen(modifier: Modifier) {
    val context = LocalContext.current
    var drawerOffset by remember { mutableFloatStateOf(1f) }
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }

    val animatedOffset by animateFloatAsState(
        targetValue = drawerOffset,
        label = "drawer_offset"
    )

    LaunchedEffect(Unit) {
        installedApps = getInstalledApps(context.packageManager)
    }

    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        drawerOffset = if (drawerOffset < 0.5f) 0f else 1f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        val newOffset = drawerOffset + (dragAmount / size.height)
                        drawerOffset = newOffset.coerceIn(0f, 1f)
                    }
                )
            }
    ) {
        // Full screen home image
        Image(
            painter = painterResource(id = R.drawable.abhishek),
            contentDescription = "Home Screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // App drawer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = size.height * animatedOffset
                }
                .background(
                    Color.Black.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top bar with time and battery
                TopBar()

                // Apps grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(installedApps) { app ->
                        AppItem(app = app) {
                            launchApp(context, app.packageName)
                        }
                    }
                }
            }
        }

        // Drag indicator when drawer is closed
        if (animatedOffset > 0.8f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.5f))
            )
        }
    }
}
