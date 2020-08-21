package com.kirkbushman.sampleapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_installed.*

class InstalledActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installed)

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        val app = TestApplication.instance
        val authClient = app.authClient

        if (authClient != null && authClient.hasSavedBearer()) {

            val bearer = authClient.getSavedBearer()
            app.setBearer(bearer)

            val intent = Intent(this, TokenInfoActivity::class.java)
            startActivity(intent)
        } else {

            browser.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {

                    if (authClient!!.isRedirectedUrl(url)) {
                        browser.stopLoading()

                        doAsync(
                            doWork = {

                                Log.i("Response URL", url)

                                val bearer = authClient.getTokenBearer(url)
                                Log.i("SUCCESS", bearer?.toString() ?: "The bearer is null")

                                app.setBearer(bearer!!)

                                val intent = Intent(this@InstalledActivity, TokenInfoActivity::class.java)
                                startActivity(intent)
                            }
                        )
                    }
                }
            }

            browser.clearFormData()
            browser.loadUrl(authClient!!.provideAuthorizeUrl())
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
