package com.juniperphoton.projecto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.juniperphoton.projecto.extension.getVersionName
import com.juniperphoton.projecto.extension.startActivitySafely

class AboutFragment : BottomSheetDialogFragment() {
    private var contentView: View? = null
    private var contentHeight = 0

    @BindView(R.id.version_text)
    lateinit var versionTextView: TextView

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        contentView = inflater.inflate(R.layout.fragment_about, container, false)
        ButterKnife.bind(this, contentView!!)

        val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        contentView!!.measure(spec, spec)
        contentHeight = contentView!!.measuredHeight

        updateVersion()

        return contentView
    }

    private fun updateVersion() {
        versionTextView.text = "Version ${context?.getVersionName()}"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        try {
            val parent = contentView?.parent
            if (parent is View) {
                parent.background = null
                val bottomSheetBehavior = BottomSheetBehavior.from(parent)
                bottomSheetBehavior.peekHeight = contentHeight
                bottomSheetBehavior.isHideable = true
            }
        } catch (e: IllegalArgumentException) {
            // ignore it
        }
    }

    @OnClick(R.id.email_item)
    internal fun onClickEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_url)))

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ScreenDecor feedback")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")

        context?.startActivitySafely(emailIntent)
    }

    @OnClick(R.id.github_item)
    internal fun onClickGitHub() {
        val uri = Uri.parse(getString(R.string.github_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context?.startActivitySafely(intent)
    }

    @OnClick(R.id.twitter_item)
    internal fun onClickTwitter() {
        val uri = Uri.parse(getString(R.string.twitter_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context?.startActivitySafely(intent)
    }

    @OnClick(R.id.weibo_item)
    internal fun onClickWeibo() {
        val uri = Uri.parse(getString(R.string.weibo_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context?.startActivitySafely(intent)
    }
}