package com.kirkbushman.sampleapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_info.*
import java.text.SimpleDateFormat
import java.util.*

class TokenInfoActivity : AppCompatActivity() {

    private val bearer by lazy { TestApplication.instance.getBearer() ?: throw IllegalStateException("Bearer not found!") }
    private val revokedErrorDialog by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle("Action not available!")
            .setMessage("The token was revoked, and no action can be done upon it anymore.")
            .setPositiveButton("Ok", null)
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        token_info.text = bearer.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_renew) {

            if (bearer.isRevoked()) {

                revokedErrorDialog.show()
            } else {

                doAsync(doWork = {
                    bearer.renewToken()
                }, onPost = {
                    val now = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                    val newText = "$bearer, Refreshed $now"

                    token_info.text = newText

                    Toast.makeText(this, "Token refreshed successfully", Toast.LENGTH_LONG).show()
                })
            }

            return true
        } else if (item.itemId == R.id.action_revoke) {

            if (bearer.isRevoked()) {

                revokedErrorDialog.show()
            } else {

                doAsync(doWork = {
                    bearer.revokeToken()
                }, onPost = {
                    val now = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
                    val newTest = "Token was revoked $now"

                    token_info.text = newTest

                    Toast.makeText(this, "Token revoked successfully", Toast.LENGTH_LONG).show()
                })
            }

            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
