package com.kirkbushman.sampleapp.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kirkbushman.auth.RedditAuth
import com.kirkbushman.auth.managers.SharedPrefsStorageManager
import com.kirkbushman.auth.models.bearers.TokenBearer
import com.kirkbushman.auth.models.creds.ApplicationCredentials
import com.kirkbushman.auth.models.creds.ScriptCredentials
import com.kirkbushman.auth.models.creds.UserlessCredentials
import com.kirkbushman.auth.models.enums.AuthType
import com.kirkbushman.auth.utils.Utils
import com.kirkbushman.sampleapp.R
import com.kirkbushman.sampleapp.module.TestCredentials
import com.kirkbushman.sampleapp.utils.DoAsync
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_info.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.Exception

@AndroidEntryPoint
class TokenInfoActivity : AppCompatActivity() {

    @Inject
    lateinit var credentials: TestCredentials
    @Inject
    lateinit var prefs: SharedPrefsStorageManager

    private var bearer: TokenBearer? = null

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

        RedditAuth
            .Saved(prefs)
            .retrieve(
                provideCredentials = {

                    when (it) {

                        AuthType.INSTALLED_APP ->
                            ApplicationCredentials(
                                clientId = credentials.clientId,
                                redirectUrl = credentials.redirectUrl
                            )

                        AuthType.USERLESS ->
                            UserlessCredentials(
                                clientId = credentials.clientId,
                                deviceId = Utils.getDeviceUUID()
                            )

                        AuthType.SCRIPT ->
                            ScriptCredentials(
                                clientId = credentials.scriptClientId,
                                clientSecret = credentials.scriptClientSecret,
                                username = credentials.username,
                                password = credentials.password
                            )

                        else -> null
                    }
                },
                onFound = { _, bearer ->

                    this.bearer = bearer
                },
                onMiss = {
                    bearer = null
                }
            )

        bindToken(bearer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (bearer == null) {
            return super.onOptionsItemSelected(item)
        }

        return when (item.itemId) {

            android.R.id.home -> { onBackPressed(); true }
            R.id.action_renew -> { tokenRenew(); true }
            R.id.action_revoke -> { tokenRevoke(); true }

            else -> super.onOptionsItemSelected(item)
        }
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

    private fun tokenRenew() {

        if (bearer!!.isRevoked()) {

            revokedErrorDialog.show()
        } else {

            var exception: Exception? = null

            DoAsync(
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
    }

    private fun tokenRevoke() {

        var exception: Exception? = null

        if (bearer!!.isRevoked()) {

            revokedErrorDialog.show()
        } else {

            DoAsync(
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
    }
}
