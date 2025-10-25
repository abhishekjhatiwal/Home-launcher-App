package com.example.homelauncherapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.text.SimpleDateFormat
import java.util.*

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: android.graphics.Bitmap
)

@Composable
fun HomeLauncherScreen() {
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
        modifier = Modifier
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

@Composable
fun TopBar() {
    var currentTime by remember { mutableStateOf(getCurrentTime()) }
    val context = LocalContext.current
    val batteryLevel = getBatteryLevel(context)

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTime = getCurrentTime()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "$batteryLevel%",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AppItem(app: AppInfo, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Image(
            bitmap = app.icon.asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.label,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(72.dp)
        )
    }
}

@SuppressLint("QueryPermissionsNeeded")
fun getInstalledApps(packageManager: PackageManager): List<AppInfo> {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val apps = packageManager.queryIntentActivities(intent, 0)
    return apps.mapNotNull { resolveInfo ->
        try {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            AppInfo(
                label = appInfo.loadLabel(packageManager).toString(),
                packageName = appInfo.packageName,
                icon = appInfo.loadIcon(packageManager).toBitmap(96, 96)
            )
        } catch (e: Exception) {
            null
        }
    }.sortedBy { it.label.lowercase() }
}

fun launchApp(context: android.content.Context, packageName: String) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.let { context.startActivity(it) }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

fun getBatteryLevel(context: android.content.Context): Int {
    val batteryManager = context.getSystemService(android.content.Context.BATTERY_SERVICE) as BatteryManager
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}