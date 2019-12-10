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

            val intent = Intent(this, InstalledActivity::class.java)
            startActivity(intent)
        }

        script_bttn.setOnClickListener {

            val app = TestApplication.instance
            app.loadClient(AuthType.SCRIPT)

            var bearer: TokenBearer? = null
            val authClient = app.authClient

            if (authClient != null && authClient.hasSavedBearer()) {

                bearer = authClient.getSavedBearer()
                app.setBearer(bearer)

                val intent = Intent(this, TokenInfoActivity::class.java)
                startActivity(intent)
            } else {

                doAsync(doWork = {

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
    }
}
