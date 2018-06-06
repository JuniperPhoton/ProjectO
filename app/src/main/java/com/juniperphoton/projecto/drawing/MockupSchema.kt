package com.juniperphoton.projecto.drawing

import android.support.annotation.DrawableRes
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

        fun createFlat(): MockupSchema {
            return MockupSchema().apply {
                shellRes = R.drawable.flat
                inScreenAspectRatio = 9f / 19f
                leftPercentage = 18f / 389
                topPercentage = 20f / 794
            }
        }
    }

    @DrawableRes
    var shellRes: Int = 0

    var filePath: String? = null

    val res: Any?
        get() {
            if (shellRes != 0) return shellRes
            return filePath
        }

    var inScreenAspectRatio: Float = 1f

    var leftPercentage = 0f
    var topPercentage = 0f
}