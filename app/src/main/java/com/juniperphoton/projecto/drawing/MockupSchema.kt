package com.juniperphoton.projecto.drawing

import androidx.annotation.DrawableRes
import com.juniperphoton.projecto.R

class MockupSchema {
    companion object {
        fun createDefault(): MockupSchema {
            return MockupSchema().apply {
                shellRes = R.drawable.shell
                inScreenAspectRatio = 9f / 19f
                leftPercentage = 32f / 740
                topPercentage = 30f / 1513
            }
        }

        fun createNoBang(): MockupSchema {
            return MockupSchema().apply {
                shellRes = R.drawable.shell_no_bang
                inScreenAspectRatio = 9f / 19f
                leftPercentage = 32f / 740
                topPercentage = 30f / 1513
            }
        }

        fun createFlat(): MockupSchema {
            return MockupSchema().apply {
                shellRes = R.drawable.flat
                inScreenAspectRatio = 9f / 19f
                leftPercentage = 18f / 389
                topPercentage = 20f / 794
            }
        }
    }

    /**
     * Use as built-in shell
     */
    @DrawableRes
    var shellRes: Int = 0

    /**
     * Use as external shell
     */
    var filePath: String? = null

    /**
     * Actual resource to be decoded. See [MockupView.DecodeTask] for details.
     */
    val res: Any?
        get() {
            if (shellRes != 0) return shellRes
            return filePath
        }

    /**
     * Indicate the aspect ratio of the phone screen
     */
    var inScreenAspectRatio: Float = 1f

    /**
     * The left position of the screenshot to the shell. In percentage.
     */
    var leftPercentage = 0f

    /**
     * The top position of the screenshot to the shell. In percentage.
     */
    var topPercentage = 0f
}