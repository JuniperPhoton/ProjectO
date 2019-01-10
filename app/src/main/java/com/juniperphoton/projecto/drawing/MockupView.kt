package com.juniperphoton.projecto.drawing

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.juniperphoton.projecto.R
import java.io.File
import java.io.FileOutputStream

class MockupView(context: Context,
                 attributeSet: AttributeSet?) : View(context, attributeSet) {
    companion object {
        private const val TAG = "MockupView"
        private const val SHADOW_BLUR_RADIUS = 80
        private const val SHELL_CONTENT_SIZE_RATIO = 0.8
        private const val OUTPUT_HEIGHT = (2240 / SHELL_CONTENT_SIZE_RATIO).toInt()
        private const val OUTPUT_RATIO = 3f / 4
    }

    /**
     * A bitmap that contains the shell.
     */
    private var shellBitmap: Bitmap? = null

    /**
     * A bitmap that contains the shadow.
     */
    private var shadowBitmap: Bitmap? = null

    /**
     * A bitmap that contains the screenshot
     */
    private var screenshotBitmap: Bitmap? = null

    private var blurBitmap: Bitmap? = null

    /**
     * A paint that draws the screenshot to provide dither feature.
     */
    private var screenshotPaint = Paint()

    /**
     * A paint that draws screenshot placeholder.
     * The default color is white.
     */
    private var blankPaint = Paint()

    /**
     * A paint that used to extract alpha channel of [shellBitmap].
     */
    private var alphaPaint = Paint()

    /**
     * A paint that draws shadow.
     */
    private var shadowPaint = Paint()

    private var dstRect = Rect()
    private var shadowRect = Rect()
    private var screenRect = Rect()
    private var blurRect = RectF()

    private var defaultBackgroundInt: Int = 0

    var onLoadingChanged: ((Boolean) -> Unit)? = null

    /**
     * The mockup schema that represents the meta of the shell.
     */
    var mockupSchema: MockupSchema? = null
        set(value) {
            field = value
            prepareBitmaps()
        }

    /**
     * Invoked when the [backgroundColorInt] is changed.
     */
    var onBackgroundColorChanged: ((Int) -> Unit)? = null

    /**
     * The drawing background color.
     */
    var backgroundColorInt: Int = 0
        set(value) {
            field = value
            onBackgroundColorChanged?.invoke(value)
        }

    /**
     * Whether we should pick [backgroundColorInt] from [screenshotBitmap].
     */
    var pickBackgroundFromScreenshot: Boolean = false
        set(value) {
            field = value
            if (pickBackgroundColor()) {
                invalidate()
            }
        }

    /**
     * Draw shadow or not.
     */
    var drawShadow: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Blur the content bitmap and draw it as background.
     */
    var drawBlur: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    /**
     * The screenshot uri starts with "content://".
     */
    var screenshotUri: Uri? = null
        set(value) {
            field = value
            screenshotBitmap?.recycle()
            loadScreenshotBitmap()
        }

    init {
        defaultBackgroundInt = ContextCompat.getColor(context, R.color.backgroundGrey)
        backgroundColorInt = defaultBackgroundInt

        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        alphaPaint.isDither = true
        alphaPaint.isFilterBitmap = true
        alphaPaint.maskFilter = BlurMaskFilter(SHADOW_BLUR_RADIUS.toFloat(), BlurMaskFilter.Blur.OUTER)
        shadowPaint.color = Color.argb(0.4f, 0f, 0f, 0f)

        screenshotPaint.isAntiAlias = true
        screenshotPaint.isDither = true
        screenshotPaint.isFilterBitmap = true
        blankPaint.color = Color.WHITE
    }

    /**
     * Draw to the output bitmap and save it to the file [filePath].
     */
    fun drawOutput(filePath: String) {
        val outputBitmap = Bitmap.createBitmap(
                (OUTPUT_HEIGHT * OUTPUT_RATIO).toInt(),
                OUTPUT_HEIGHT,
                Bitmap.Config.ARGB_8888)
        val c = Canvas(outputBitmap)
        drawInternal(c, outputBitmap.width, outputBitmap.height)

        val fos = FileOutputStream(File(filePath))
        fos.use {
            outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }

        outputBitmap.recycle()
    }

    private fun pickBackgroundColor(): Boolean {
        val bm = screenshotBitmap ?: return false
        if (bm.isRecycled) return false

        val p = Palette.from(bm).generate()
        val color = p.getDominantColor(defaultBackgroundInt)
        backgroundColorInt = color

        return true
    }

    private fun loadScreenshotBitmap() {
        val uri = screenshotUri ?: return
        object : DecodeTask(context) {
            override fun onPreExecute() {
                super.onPreExecute()
                onLoadingChanged?.invoke(true)
            }

            override fun onPostExecute(result: DecodeResult?) {
                super.onPostExecute(result)
                screenshotBitmap = result?.contentBitmap
                blurBitmap = result?.blurBitmap
                pickBackgroundColor()
                invalidate()
                onLoadingChanged?.invoke(false)
            }
        }.execute(DecodeInput(uri, null, true))
    }

    private fun prepareBitmaps() {
        val schema = mockupSchema ?: return
        object : DecodeTask(context) {
            override fun onPostExecute(result: DecodeResult?) {
                super.onPostExecute(result)
                shellBitmap = result?.contentBitmap
                shadowBitmap = shellBitmap?.extractAlpha(alphaPaint, null)
                invalidate()
            }
        }.execute(DecodeInput(null, schema.res as Int, false))
    }

    override fun onDraw(canvas: Canvas) {
        drawInternal(canvas, width, height)
    }

    private fun drawInternal(canvas: Canvas, outputW: Int, outputH: Int) {
        val shellBm = shellBitmap ?: return
        val shadowBm = shadowBitmap ?: return

        val schema = mockupSchema ?: return

        if (drawBlur && blurBitmap != null) {
            val bW = blurBitmap!!.width
            val bH = blurBitmap!!.height

            val ratio = bW * 1f / bH
            blurRect.set(0f, 0f, outputW.toFloat(), outputW / ratio)
            blurRect.offset(0f, -(blurRect.height() - outputH) / 2f)

            canvas.drawBitmap(blurBitmap, null, blurRect, null)
        } else {
            canvas.drawColor(backgroundColorInt)
        }

        val shellH = outputH * SHELL_CONTENT_SIZE_RATIO
        val ratio = shellBm.width * 1f / shellBm.height
        val shellW = shellH * ratio

        val l = ((outputW - shellW) / 2).toInt()
        val t = ((outputH - shellH) / 2).toInt()
        dstRect.set(l, t, (l + shellW).toInt(), (t + shellH).toInt())

        Log.i(TAG, "outputW: $outputW, outputH: $outputH, dst rect: $dstRect")

        shadowRect.set(dstRect)
        shadowRect.inset(-SHADOW_BLUR_RADIUS / 3, -SHADOW_BLUR_RADIUS / 3)

        if (drawShadow) {
            canvas.drawBitmap(shadowBm, null, shadowRect, shadowPaint)
        }

        val shouldDrawScreenshot = screenshotBitmap?.isRecycled == false
        val screenRatio = if (shouldDrawScreenshot) {
            screenshotBitmap!!.width * 1f / screenshotBitmap!!.height
        } else {
            schema.inScreenAspectRatio
        }

        screenRect.set(dstRect)
        screenRect.inset((shellW * schema.leftPercentage).toInt(), 0)
        screenRect.top += (shellH * schema.topPercentage).toInt()
        screenRect.bottom = (screenRect.top + (screenRect.width() / screenRatio)).toInt()

        if (shouldDrawScreenshot) {
            canvas.drawBitmap(screenshotBitmap, null, screenRect, screenshotPaint)
        } else {
            canvas.drawRect(screenRect, blankPaint)
        }

        canvas.drawBitmap(shellBm, null, dstRect, null)
    }
}