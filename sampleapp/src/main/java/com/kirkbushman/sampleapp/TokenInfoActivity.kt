package com.kirkbushman.sampleapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kirkbushman.auth.models.TokenBearer
import kotlinx.android.synthetic.main.activity_info.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception

class TokenInfoActivity : AppCompatActivity() {

    private val bearer by lazy { TestApplication.instance.getBearer() }
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
            it.setDisplayHomeAsUpEnabled(true)
        }

        bindToken(bearer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {

            onBackPressed()
            return true
        }

        if (bearer == null) {
            return super.onOptionsItemSelected(item)
        }

        if (item.itemId == R.id.action_renew) {

            if (bearer!!.isRevoked()) {

                revokedErrorDialog.show()
            } else {

                var exception: Exception? = null

                doAsync(
                    doWork = {

                        try {
                            bearer!!.renewToken()
                        } catch (ex: Exception) {
                            exception = ex
                        }
                    },
                    onPost = {

                        val message = if (exception != null) {

                            "A problem occured while refreshing the token, with exception: ${exception!!.message}"
                        } else {
                            val now = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

                            "${bearer!!.getAccessToken()}, Refreshed $now"
                        }

                        bindToken(bearer, message)

                        Toast.makeText(this, "Token refreshed successfully", Toast.LENGTH_LONG).show()
                    }
                )
            }

            return true
        } else if (item.itemId == R.id.action_revoke) {

            var exception: Exception? = null

            if (bearer!!.isRevoked()) {

                revokedErrorDialog.show()
            } else {

                doAsync(
                    doWork = {

                        try {
                            bearer!!.revokeToken()
                        } catch (ex: Exception) {
                            exception = ex
                        }
                    },
                    onPost = {

                        val message = if (exception != null) {

                            "A problem has occurred while revoking the token, with message: ${exception!!.message}"
                        } else {
                            val now = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

                            "Token was revoked $now"
                        }

                        bindToken(bearer, message)

                        Toast.makeText(this, "Token revoked successfully", Toast.LENGTH_LONG).show()
                    }
                )
            }

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun bindToken(bearer: TokenBearer?, message: String? = null) {

        if (message != null) {

            token_info_message.visibility = View.VISIBLE
            token_info_message.text = message
        } else {
            token_info_message.visibility = View.GONE
        }

        val isAuthedText = "IsAuthed: ${bearer?.isAuthed() ?: false}"
        token_info_is_authed.text = isAuthedText

        val authTypeText = "AuthType: ${bearer?.getAuthType() ?: "NONE"}"
        token_info_auth_type.text = authTypeText

        val token = bearer?.getToken()
        if (token != null) {

            val accessTokenText = "AccessToken: ${token.accessToken}"
            token_info_access_token.text = accessTokenText
            val refreshTokenText = "RefreshToken: ${token.refreshToken}"
            token_info_refresh_token.text = refreshTokenText
            val tokenTypeText = "TokenType: ${token.tokenType}"
            token_info_token_type.text = tokenTypeText
            val expiresInText = "ExpiresIn: ${token.expirationTime}"
            token_info_expires_in.text = expiresInText
            val createdTimeText = "CreatedTime: ${token.createdTime}"
            token_info_created_time.text = createdTimeText
            val scopesText = "Scopes: ${token.scopes}"
            token_info_scopes.text = scopesText
        } else {

            token_info_access_token.text = ""
            token_info_refresh_token.text = ""
            token_info_token_type.text = ""
            token_info_expires_in.text = ""
            token_info_created_time.text = ""
            token_info_scopes.text = ""
        }
    }
}
