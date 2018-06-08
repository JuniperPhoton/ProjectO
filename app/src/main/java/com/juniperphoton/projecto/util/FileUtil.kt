package com.juniperphoton.projecto.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.juniperphoton.projecto.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {
    private const val TAG = "FileUtil"

    fun getOutputMediaFile(context: Context): File? {
        val mediaStorageDir = getMediaStorageDir(context)
        if (mediaStorageDir == null) {
            Log.e(TAG, "Error failed to create directory")
            return null
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return File(mediaStorageDir + File.separator + "DECOR_" + timeStamp + ".png")
    }

    private fun getMediaStorageDir(context: Context): String? {
        val folderName = context.resources.getString(R.string.app_name)

        val mediaStorageDir: File
        mediaStorageDir = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) ?: return ""
            File(path, folderName)
        } else {
            val extStorageDirectory = context.filesDir.absolutePath
            File(extStorageDirectory, folderName)
        }

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.i(TAG, "Error failed to create directory")
                return null
            }
        }

        return mediaStorageDir.absolutePath
    }
}