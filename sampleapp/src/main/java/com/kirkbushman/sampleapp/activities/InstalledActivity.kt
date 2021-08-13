package com.kirkbushman.sampleapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.kirkbushman.auth.AppAuth
import com.kirkbushman.sampleapp.databinding.ActivityInstalledBinding
import com.kirkbushman.sampleapp.utils.DoAsync
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InstalledActivity : AppCompatActivity() {

    @Inject
    lateinit var appAuth: AppAuth

    lateinit var binding: ActivityInstalledBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInstalledBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        if (appAuth.hasSavedBearer()) {

            val intent = Intent(this, TokenInfoActivity::class.java)
            startActivity(intent)
        } else {

            binding.browser.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {

                    val isRedirected = appAuth.isRedirectedUrl(url)
                    if (isRedirected) {
                        binding.browser.stopLoading()

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

            binding.browser.clearFormData()
            binding.browser.loadUrl(appAuth.provideAuthorizeUrl())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        binding.browser.removeAllViews()
        binding.browser.destroy()
    }
}
