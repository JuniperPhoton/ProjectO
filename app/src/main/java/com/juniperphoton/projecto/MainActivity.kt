@file:Suppress("DEPRECATION")

package com.juniperphoton.projecto

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.ProgressDialog
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.juniperphoton.projecto.drawing.MockupSchema
import com.juniperphoton.projecto.extension.isLightColor
import com.juniperphoton.projecto.util.FileUtil
import com.juniperphoton.projecto.util.NotificationUtil
import com.juniperphoton.projecto.util.PermissionUtil
import com.juniperphoton.projecto.util.Preferences
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val PICK_IMAGE_CODE = 0
        private const val WRITE_EXTERNAL_STORAGE_CODE = 1
        private const val TAG = "MainActivity"
    }

    private var progressDialog: ProgressDialog? = null

    private val mockups = listOf(MockupSchema.create7Pro())

    private var mockupsIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateStatusBarColor(mockupView.backgroundColorInt)

        mockupView.onBackgroundColorChanged = changed@{ color ->
            updateStatusBarColor(color)
        }

        mockupView.onLoadingChanged = { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        val pick = Preferences.getBoolean(this, Preferences.KEY_PICK, false)
        mockupView.pickBackgroundFromScreenshot = pick
        pickButton.setImageLevel(if (pick) 1 else 0)

        val drawBlur = Preferences.getBoolean(this, Preferences.KEY_SHADOW, false)
        mockupView.drawBlur = drawBlur
        blurButton.setImageLevel(if (drawBlur) 1 else 0)

        mockupsIndex = Preferences.getInt(this, Preferences.KEY_FRAME, 0)

        mockupView.mockupSchema = mockups[mockupsIndex]

        updateScreenshotByIntent()

        moreButton.setOnClickListener(this)
        outputFab.setOnClickListener(this)
        mockupView.setOnClickListener(this)
        pickButton.setOnClickListener(this)
        blurButton.setOnClickListener(this)
        frameButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.moreButton -> {
                AboutFragment().show(supportFragmentManager, "About")
            }
            R.id.outputFab -> {
                compose()
            }
            R.id.mockupView -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select screenshot"), PICK_IMAGE_CODE)
            }
            R.id.pickButton -> {
                mockupView.pickBackgroundFromScreenshot = !mockupView.pickBackgroundFromScreenshot
                pickButton.setImageLevel(if (mockupView.pickBackgroundFromScreenshot) 1 else 0)
                Preferences.setBoolean(this, Preferences.KEY_PICK, mockupView.pickBackgroundFromScreenshot)
            }
            R.id.blurButton -> {
                mockupView.drawBlur = !mockupView.drawBlur
                blurButton.setImageLevel(if (mockupView.drawBlur) 1 else 0)
                Preferences.setBoolean(this, Preferences.KEY_BLUR, mockupView.drawBlur)
            }
            R.id.frameButton -> {
                mockupsIndex++
                if (mockupsIndex >= mockups.size) {
                    mockupsIndex = 0
                }
                mockupView.mockupSchema = mockups[mockupsIndex]
                Preferences.setInt(this, Preferences.KEY_FRAME, mockupsIndex)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        updateScreenshotByIntent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            PICK_IMAGE_CODE -> {
                data?.data?.let {
                    onPickedImage(it)
                }
            }
            else -> {

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_CODE) {
            compose()
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun updateStatusBarColor(color: Int) {
        val flag = if (color.isLightColor()) {
            SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            0
        }

        window.decorView.systemUiVisibility = flag
        window.statusBarColor = color

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && color.isLightColor()) {
            val decorView = window?.decorView ?: return
            val mask = decorView.systemUiVisibility
            decorView.systemUiVisibility = mask or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    private fun updateScreenshotByIntent() {
        intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let {
            onPickedImage(it)
        }
    }

    private fun onPickedImage(dataUri: Uri) {
        Log.i(TAG, "pick image: $dataUri")
        addHint.visibility = View.GONE
        mockupView.screenshotUri = dataUri
    }

    private fun compose() {
        when {
            PermissionUtil.isGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                val file = FileUtil.getOutputMediaFile(this) ?: return

                object : AsyncTask<String, Any, Pair<File, Bitmap>>() {
                    override fun onPreExecute() {
                        progressDialog = ProgressDialog(this@MainActivity)
                        progressDialog?.setTitle("Rendering")
                        progressDialog?.setMessage("Please wait until it's done")
                        progressDialog?.show()
                    }

                    override fun doInBackground(vararg params: String?): Pair<File, Bitmap>? {
                        mockupView.drawOutput(file.absolutePath)
                        val o = BitmapFactory.Options()
                        o.inSampleSize = 4
                        val bm = BitmapFactory.decodeFile(file.path, o)
                        return file to bm
                    }

                    override fun onPostExecute(result: Pair<File, Bitmap>?) {
                        progressDialog?.dismiss()

                        result ?: return

                        val file = result.first
                        val bm = result.second

                        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(file)))

                        sendNotificationOnSaved(file, bm)
                    }
                }.execute(file.absolutePath)
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        WRITE_EXTERNAL_STORAGE_CODE)
            }
        }
    }

    private fun sendNotificationOnSaved(file: File, bitmap: Bitmap) {
        val builder = NotificationCompat.Builder(this, NotificationUtil.DEFAULT_NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle("Saved")
        builder.setContentText("Click to open file: ${file.path}")
        builder.setSmallIcon(R.drawable.ic_saved)
        builder.setLargeIcon(bitmap)

        val intent = Intent()
        intent.action = ACTION_VIEW
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(FileProvider.getUriForFile(this, packageName, file), "image/*")

        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT))

        val notification = builder.build()
        NotificationUtil.show(notification)
    }
}
