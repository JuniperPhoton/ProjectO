package com.juniperphoton.projecto.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.AsyncTask
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.juniperphoton.projecto.R

class MockupView(context: Context,
                 attributeSet: AttributeSet?) : View(context, attributeSet) {
    companion object {
        private const val TAG = "MockupView"
        private const val MAX_PREVIEW_SIZE = 1080
    }

    private var shellBitmap: Bitmap? = null

    init {
        loadShellBitmap()
    }

    private fun loadShellBitmap() {
        val o = object : AsyncTask<Any, Any, Bitmap?>() {
            override fun onPostExecute(result: Bitmap?) {
                super.onPostExecute(result)
                shellBitmap = result
                invalidate()
            }

            override fun doInBackground(vararg params: Any?): Bitmap? {
                val o = BitmapFactory.Options()
                o.inJustDecodeBounds = true
                BitmapFactory.decodeResource(context.resources, R.drawable.shell, o)

                o.inJustDecodeBounds = false

                o.inSampleSize = Math.max(1, o.outWidth / MAX_PREVIEW_SIZE)

                return BitmapFactory.decodeResource(context.resources, R.drawable.shell, o)
            }
        }
        o.execute(null)
    }

    override fun onDraw(canvas: Canvas) {
        val shellBm = shellBitmap ?: return

        canvas.drawColor(ContextCompat.getColor(context, R.color.backgroundGrey))
        canvas.drawBitmap(shellBm, 0f, 0f, null)
    }
}