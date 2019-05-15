package com.juniperphoton.projecto

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.juniperphoton.projecto.extension.getVersionName
import com.juniperphoton.projecto.extension.startActivitySafely

class AboutFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private var contentView: View? = null

    lateinit var versionTextView: TextView

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        contentView = inflater.inflate(R.layout.fragment_about, container, false)

        updateVersion()

        return contentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.email_item).setOnClickListener(this)
        view.findViewById<View>(R.id.github_item).setOnClickListener(this)
        view.findViewById<View>(R.id.twitter_item).setOnClickListener(this)
        view.findViewById<View>(R.id.weibo_item).setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun updateVersion() {
        versionTextView.text = "Version ${context?.getVersionName()}"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Looper.myQueue().addIdleHandler {
            try {
                val parent = contentView?.parent
                if (parent is View) {
                    parent.background = null
                    val bottomSheetBehavior = BottomSheetBehavior.from(parent)
                    bottomSheetBehavior.peekHeight = parent.height
                    bottomSheetBehavior.isHideable = true
                }
            } catch (e: IllegalArgumentException) {
                // ignore it
            }
            false
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.email_item -> {
                onClickEmail()
            }
            R.id.github_item -> {
                onClickGitHub()
            }
            R.id.twitter_item -> {
                onClickTwitter()
            }
            R.id.weibo_item -> {
                onClickWeibo()
            }
        }
    }

    private fun onClickEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_url)))

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ScreenDecor feedback")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")

        context?.startActivitySafely(emailIntent)
    }

    private fun onClickGitHub() {
        val uri = Uri.parse(getString(R.string.github_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context?.startActivitySafely(intent)
    }

    private fun onClickTwitter() {
        val uri = Uri.parse(getString(R.string.twitter_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context?.startActivitySafely(intent)
    }

    private fun onClickWeibo() {
        val uri = Uri.parse(getString(R.string.weibo_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context?.startActivitySafely(intent)
    }
}