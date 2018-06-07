@file:Suppress("DEPRECATION")

package com.juniperphoton.projecto

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.juniperphoton.projecto.drawing.MockupSchema
import com.juniperphoton.projecto.drawing.MockupView
import com.juniperphoton.projecto.extension.isLightColor
import com.juniperphoton.projecto.util.FileUtil
import com.juniperphoton.projecto.util.PermissionUtil
import com.juniperphoton.projecto.util.Preferences

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PICK_IMAGE_CODE = 0
        private const val WRITE_EXTERNAL_STORAGE_CODE = 1
        private const val TAG = "MainActivity"
    }

    private lateinit var mockupView: MockupView
    private lateinit var mockupContainer: View
    private lateinit var pickButton: ImageView
    private lateinit var shadowButton: ImageView
    private lateinit var frameButton: ImageView
    private lateinit var addHint: View
    private lateinit var fab: FloatingActionButton

    private var progressDialog: ProgressDialog? = null

    private var defaultMockup = MockupSchema.createDefault()
    private var flatMockup = MockupSchema.createFlat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mockupView = findViewById(R.id.mockup_view)
        mockupContainer = findViewById(R.id.mockup_container)
        addHint = findViewById(R.id.add_hint)
        pickButton = findViewById(R.id.pick_button)
        shadowButton = findViewById(R.id.shadow_button)
        frameButton = findViewById(R.id.frame_button)

        fab = findViewById(R.id.output_fab)

        fab.setOnClickListener {
            output()
        }

        mockupView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select screenshot"), PICK_IMAGE_CODE)
        }
        mockupView.onBackgroundColorChanged = changed@{ color ->
            window.statusBarColor = color
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && color.isLightColor()) {
                val decorView = window?.decorView ?: return@changed
                val mask = decorView.systemUiVisibility
                decorView.systemUiVisibility = mask or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }

        pickButton.setOnClickListener {
            mockupView.pickBackgroundFromScreenshot = !mockupView.pickBackgroundFromScreenshot
            pickButton.setImageLevel(if (mockupView.pickBackgroundFromScreenshot) 1 else 0)
            Preferences.setBoolean(this, Preferences.KEY_PICK, mockupView.pickBackgroundFromScreenshot)
        }

        shadowButton.setOnClickListener {
            mockupView.drawShadow = !mockupView.drawShadow
            shadowButton.setImageLevel(if (mockupView.drawShadow) 1 else 0)
            Preferences.setBoolean(this, Preferences.KEY_SHADOW, mockupView.drawShadow)
        }

        frameButton.setOnClickListener {
            if (mockupView.mockupSchema == defaultMockup) {
                mockupView.mockupSchema = flatMockup
            } else {
                mockupView.mockupSchema = defaultMockup
            }
            val checked = mockupView.mockupSchema == flatMockup
            frameButton.setImageLevel(if (checked) 1 else 0)
            Preferences.setBoolean(this, Preferences.KEY_FRAME, checked)
        }

        // todo: solve this
        frameButton.visibility = View.GONE

        val pick = Preferences.getBoolean(this, Preferences.KEY_PICK, false)
        mockupView.pickBackgroundFromScreenshot = pick
        pickButton.setImageLevel(if (pick) 1 else 0)

        val drawShadow = Preferences.getBoolean(this, Preferences.KEY_SHADOW, false)
        mockupView.drawShadow = drawShadow
        shadowButton.setImageLevel(if (drawShadow) 1 else 0)

//        val frame = Preferences.getBoolean(this, Preferences.KEY_FRAME, false)
//        mockupView.mockupSchema = if (frame) flatMockup else defaultMockup
//        frameButton.setImageLevel(if (mockupView.mockupSchema == flatMockup) 1 else 0)

        mockupView.mockupSchema = defaultMockup

        updateScreenshotByIntent()
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
            output()
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
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

    private fun output() {
        when {
            PermissionUtil.isGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                val file = FileUtil.getOutputMediaFile(this) ?: return

                object : AsyncTask<String, Any, Boolean>() {
                    override fun onPreExecute() {
                        progressDialog = ProgressDialog(this@MainActivity)
                        progressDialog?.setTitle("DRAWING")
                        progressDialog?.setMessage("Please don't leave the app")
                        progressDialog?.show()
                    }

                    override fun doInBackground(vararg params: String?): Boolean {
                        mockupView.drawOutput(file.absolutePath)
                        return true
                    }

                    override fun onPostExecute(result: Boolean?) {
                        progressDialog?.dismiss()

                        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(file)))
                        Toast.makeText(this@MainActivity, "Saved:D", Toast.LENGTH_SHORT).show()
                    }
                }.execute(file.absolutePath)
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        WRITE_EXTERNAL_STORAGE_CODE)
            }
        }
    }
}
