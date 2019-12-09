package com.kirkbushman.sampleapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.kirkbushman.auth.models.AuthType
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

        TestApplication.instance.loadClient(AuthType.INSTALLED_APP)
        val authClient = TestApplication.instance.authClient

        if (authClient != null && authClient.hasSavedBearer()) {

            val bearer = authClient.getSavedBearer()
            TestApplication.instance.setBearer(bearer)

            val intent = Intent(this, TokenInfoActivity::class.java)
            startActivity(intent)
        } else {

            CookieManager.getInstance().removeAllCookies(null)
            browser.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {

                    if (authClient!!.isRedirectedUrl(url)) {
                        browser.stopLoading()

                        doAsync(doWork = {

                            Log.i("Response URL", url)

                            val bearer = authClient.getTokenBearer(url)
                            Log.i("SUCCESS", bearer?.toString() ?: "The bearer is null")

                            TestApplication.instance.setBearer(bearer!!)

                            val intent = Intent(this@InstalledActivity, TokenInfoActivity::class.java)
                            startActivity(intent)
                        })
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
