package com.example.homelauncherapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import androidx.core.graphics.drawable.toBitmap
import com.example.homelauncherapp.data.AppInfo
import java.text.SimpleDateFormat
import java.util.*

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