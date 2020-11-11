package com.kirkbushman.sampleapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kirkbushman.auth.ScriptAuth
import com.kirkbushman.auth.UserlessAuth
import com.kirkbushman.sampleapp.R
import com.kirkbushman.sampleapp.utils.DoAsync
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userlessAuth: UserlessAuth
    @Inject
    lateinit var scriptAuth: ScriptAuth

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

        userless_bttn.setOnClickListener {

            if (userlessAuth.hasSavedBearer()) {

                val intent = Intent(this, TokenInfoActivity::class.java)
                startActivity(intent)
            } else {

                DoAsync(
                    doWork = { userlessAuth.authenticate() },
                    onPost = {

                        val intent = Intent(this, TokenInfoActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }

        script_bttn.setOnClickListener {

            if (scriptAuth.hasSavedBearer()) {

                val intent = Intent(this, TokenInfoActivity::class.java)
                startActivity(intent)
            } else {

                DoAsync(
                    doWork = { scriptAuth.authenticate() },
                    onPost = {

                        val intent = Intent(this, TokenInfoActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }

        token_info_bttn.setOnClickListener {

            val intent = Intent(this, TokenInfoActivity::class.java)
            startActivity(intent)
        }
    }
}
