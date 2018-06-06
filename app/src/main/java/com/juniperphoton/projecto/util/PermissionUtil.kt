package com.juniperphoton.projecto.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

/**
 * Util class for handling permission request, target for Android M.
 */
object PermissionUtil {
    private const val REQUEST_CODE = 0

    /**
     * Check if the specified [permission] is granted.
     */
    fun isGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checked if all [permissions] are granted.
     */
    fun isAllGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if there is one permission in [permissions] is disabled by user, which means that
     * we can't prompt the permission request dialog again.
     */
    fun anyNeverAskAgain(activity: Activity, permissions: Array<String>): Boolean {
        return permissions.any {
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, it) &&
                    !isGranted(activity, it)
        }
    }
}