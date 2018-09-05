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
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.widget.ImageView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
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

    @BindView(R.id.mockup_view)
    lateinit var mockupView: MockupView

    @BindView(R.id.mockup_container)
    lateinit var mockupContainer: View

    @BindView(R.id.pick_button)
    lateinit var pickButton: ImageView

    @BindView(R.id.shadow_button)
    lateinit var shadowButton: ImageView

    @BindView(R.id.frame_button)
    lateinit var frameButton: ImageView

    @BindView(R.id.add_hint)
    lateinit var addHint: View

    private var progressDialog: ProgressDialog? = null

    private val mockups = listOf(MockupSchema.createDefault(),
            MockupSchema.createNoBang(),
            MockupSchema.createFlat())

    private var mockupsIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)

        updateStatusBarColor(mockupView.backgroundColorInt)

        mockupView.onBackgroundColorChanged = changed@{ color ->
            updateStatusBarColor(color)
        }

        val pick = Preferences.getBoolean(this, Preferences.KEY_PICK, false)
        mockupView.pickBackgroundFromScreenshot = pick
        pickButton.setImageLevel(if (pick) 1 else 0)

        val drawShadow = Preferences.getBoolean(this, Preferences.KEY_SHADOW, false)
        mockupView.drawShadow = drawShadow
        shadowButton.setImageLevel(if (drawShadow) 1 else 0)

        mockupsIndex = Preferences.getInt(this, Preferences.KEY_FRAME, 0)

        mockupView.mockupSchema = mockups[mockupsIndex]

        updateScreenshotByIntent()
    }

    @OnClick(R.id.more_button)
    fun onClickMore() {
        AboutFragment().show(supportFragmentManager, "About")
    }

    @OnClick(R.id.output_fab)
    fun onClickFab() {
        output()
    }

    @OnClick(R.id.mockup_view)
    fun onClickMockup() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select screenshot"), PICK_IMAGE_CODE)
    }

    @OnClick(R.id.pick_button)
    fun onClickPick() {
        mockupView.pickBackgroundFromScreenshot = !mockupView.pickBackgroundFromScreenshot
        pickButton.setImageLevel(if (mockupView.pickBackgroundFromScreenshot) 1 else 0)
        Preferences.setBoolean(this, Preferences.KEY_PICK, mockupView.pickBackgroundFromScreenshot)
    }

    @OnClick(R.id.shadow_button)
    fun onClickShadow() {
        mockupView.drawShadow = !mockupView.drawShadow
        shadowButton.setImageLevel(if (mockupView.drawShadow) 1 else 0)
        Preferences.setBoolean(this, Preferences.KEY_SHADOW, mockupView.drawShadow)
    }

    @OnClick(R.id.frame_button)
    fun onClickFrame() {
        mockupsIndex++
        if (mockupsIndex >= mockups.size) {
            mockupsIndex = 0
        }
        mockupView.mockupSchema = mockups[mockupsIndex]
        Preferences.setInt(this, Preferences.KEY_FRAME, mockupsIndex)
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

    private fun updateStatusBarColor(color: Int) {
        var flag = if (color.isLightColor()) {
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

    private fun output() {
        when {
            PermissionUtil.isGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                val file = FileUtil.getOutputMediaFile(this) ?: return

                object : AsyncTask<String, Any, Boolean>() {
                    override fun onPreExecute() {
                        progressDialog = ProgressDialog(this@MainActivity)
                        progressDialog?.setTitle("Rendering")
                        progressDialog?.setMessage("Please wait until it's done")
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
