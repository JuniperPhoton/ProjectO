package com.juniperphoton.projecto.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.support.annotation.DrawableRes
import android.util.Log
import com.juniperphoton.projecto.util.fastBlur

private const val TAG = "DecodeTask"
private const val MAX_PREVIEW_SIZE = 1080
private const val BLUR_RADIUS = 150

/**
 * Represent a decode result for [DecodeTask].
 */
class DecodeResult(val contentBitmap: Bitmap?, val blurBitmap: Bitmap?)

/**
 * Represent a decode input for [DecodeTask].
 */
class DecodeInput(val uri: Uri?, @DrawableRes val res: Int?, val blur: Boolean)

/**
 * Represent an async task which decodes drawable or file as bitmap and blur it.
 */
abstract class DecodeTask(private val context: Context
) : AsyncTask<DecodeInput, Any, DecodeResult?>() {
    override fun doInBackground(vararg params: DecodeInput): DecodeResult? {
        val param = params.firstOrNull() ?: return null

        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true

        val uri = param.uri
        val resId = param.res
        val blur = param.blur

        if (uri != null) {
            Log.i(TAG, "about to decode file: $uri")

            val fd = context.contentResolver.openFileDescriptor(uri, "r")
            BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, o)
        } else if (resId != null) {
            Log.i(TAG, "about to decode res: $resId")

            BitmapFactory.decodeResource(context.resources, resId, o)
        }

        o.inJustDecodeBounds = false
        o.inSampleSize = Math.max(1, o.outWidth / MAX_PREVIEW_SIZE)

        val bm = when {
            uri != null -> {
                val fd = context.contentResolver.openFileDescriptor(uri, "r")
                BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, null)
            }
            else -> BitmapFactory.decodeResource(context.resources, resId as Int, o)
        } ?: return null

        val blurred = if (blur) bm.fastBlur(BLUR_RADIUS) else null
        return DecodeResult(bm, blurred)
    }
}
