package com.juniperphoton.projecto.extension

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

fun Context.startActivitySafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}

@Suppress("unused")
fun Context.getVersionName(): String? {
    return try {
        val manager = packageManager
        val info = manager.getPackageInfo(packageName, 0)
        info.versionName
    } catch (e: Exception) {
        null
    }
}