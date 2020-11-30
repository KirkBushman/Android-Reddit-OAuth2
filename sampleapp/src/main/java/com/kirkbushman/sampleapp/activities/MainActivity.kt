package com.kirkbushman.sampleapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kirkbushman.auth.ScriptAuth
import com.kirkbushman.auth.UserlessAuth
import com.kirkbushman.sampleapp.databinding.ActivityMainBinding
import com.kirkbushman.sampleapp.utils.DoAsync
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userlessAuth: UserlessAuth
    @Inject
    lateinit var scriptAuth: ScriptAuth

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        binding.installedBttn.setOnClickListener {

            val intent = Intent(this, InstalledActivity::class.java)
            startActivity(intent)
        }

        binding.userlessBttn.setOnClickListener {

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

        binding.scriptBttn.setOnClickListener {

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

        binding.tokenInfoBttn.setOnClickListener {

            val intent = Intent(this, TokenInfoActivity::class.java)
            startActivity(intent)
        }
    }
}
