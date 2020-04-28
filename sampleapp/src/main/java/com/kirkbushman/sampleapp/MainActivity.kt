package com.kirkbushman.sampleapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kirkbushman.auth.models.AuthType
import com.kirkbushman.auth.models.TokenBearer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        installed_bttn.setOnClickListener {

            val app = TestApplication.instance
            app.loadClient(AuthType.INSTALLED_APP)

            val intent = Intent(this, InstalledActivity::class.java)
            startActivity(intent)
        }

        userless_bttn.setOnClickListener {

            val app = TestApplication.instance
            app.loadClient(AuthType.USERLESS)

            val authClient = app.authClient
            if (authClient != null && authClient.hasSavedBearer()) {

                val intent = Intent(this, TokenInfoActivity::class.java)
                startActivity(intent)
            } else {

                var bearer: TokenBearer? = null

                DoAsync(doWork = {

                    bearer = authClient?.getTokenBearer()
                }, onPost = {

                    if (bearer != null) {
                        app.setBearer(bearer!!)
                    }

                    val intent = Intent(this, TokenInfoActivity::class.java)
                    startActivity(intent)
                })
            }
        }

        script_bttn.setOnClickListener {

            val app = TestApplication.instance
            app.loadClient(AuthType.SCRIPT)

            val authClient = app.authClient
            if (authClient != null && authClient.hasSavedBearer()) {

                val intent = Intent(this, TokenInfoActivity::class.java)
                startActivity(intent)
            } else {

                var bearer: TokenBearer? = null

                DoAsync(doWork = {

                    bearer = authClient?.getTokenBearer()
                }, onPost = {

                    if (bearer != null) {
                        app.setBearer(bearer!!)
                    }

                    val intent = Intent(this, TokenInfoActivity::class.java)
                    startActivity(intent)
                })
            }
        }

        token_info_bttn.setOnClickListener {

            val app = TestApplication.instance
            app.loadClient()

            val intent = Intent(this, TokenInfoActivity::class.java)
            startActivity(intent)
        }

        token_edit_bttn.setOnClickListener {

            val app = TestApplication.instance
            app.loadClient()

            val intent = Intent(this, TokenEditActivity::class.java)
            startActivity(intent)
        }
    }
}
