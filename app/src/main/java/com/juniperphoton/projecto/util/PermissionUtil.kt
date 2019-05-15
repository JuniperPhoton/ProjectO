package com.juniperphoton.projecto.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

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
}