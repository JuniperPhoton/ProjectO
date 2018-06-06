package com.juniperphoton.projecto.util

import android.content.Context
import android.os.Environment
import android.util.Log
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
        return File(mediaStorageDir + File.separator + "IMG_" + timeStamp + ".png")
    }

    fun getMediaStorageDir(context: Context): String? {
        val mediaStorageDir: File
        mediaStorageDir = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) ?: return ""
            File(path, "ProjectO")
        } else {
            val extStorageDirectory = context.filesDir.absolutePath
            File(extStorageDirectory, "ButterCamera")
        }

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.i("FileUtil", "Error failed to create directory")
                return null
            }
        }

        return mediaStorageDir.absolutePath
    }
}