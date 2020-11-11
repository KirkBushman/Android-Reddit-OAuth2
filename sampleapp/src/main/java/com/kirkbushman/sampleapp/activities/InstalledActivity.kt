package com.kirkbushman.sampleapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.kirkbushman.auth.AppAuth
import com.kirkbushman.sampleapp.R
import com.kirkbushman.sampleapp.utils.DoAsync
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_installed.*
import javax.inject.Inject

@AndroidEntryPoint
class InstalledActivity : AppCompatActivity() {

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installed)

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        if (appAuth.hasSavedBearer()) {

            val intent = Intent(this, TokenInfoActivity::class.java)
            startActivity(intent)
        } else {

            browser.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {

                    if (appAuth.isRedirectedUrl(url)) {
                        browser.stopLoading()

                        DoAsync(
                            doWork = {

                                Log.i("Response URL", url)

                                val bearer = appAuth.authenticate(url)
                                Log.i("SUCCESS", bearer?.toString() ?: "The bearer is null")

                                val intent = Intent(this@InstalledActivity, TokenInfoActivity::class.java)
                                startActivity(intent)
                            }
                        )
                    }
                }
            }

            browser.clearFormData()
            browser.loadUrl(appAuth.provideAuthorizeUrl())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (browser != null) {
            browser.removeAllViews()
            browser.destroy()
        }
    }
}
