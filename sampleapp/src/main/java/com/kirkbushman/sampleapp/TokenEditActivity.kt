package com.kirkbushman.sampleapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kirkbushman.auth.managers.SharedPrefsStorageManager
import com.kirkbushman.auth.models.Token
import kotlinx.android.synthetic.main.activity_edit.*

class TokenEditActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_edit)

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }

        val isAuthedText = "IsAuthed: ${bearer?.isAuthed() ?: false}"
        token_info_is_authed.text = isAuthedText

        val authTypeText = "AuthType: ${bearer?.getAuthType() ?: "NONE"}"
        token_info_auth_type.text = authTypeText

        val token = bearer?.getToken()
        if (token != null) {

            token_edit_access_token.setText(token.accessToken)
            token_edit_refresh_token.setText(token.refreshToken)
            token_info_token_type.text = token.tokenType
            token_edit_created_time.setText(token.createdTime.toString())
            token_edit_expires_in.setText(token.expiresInSecs.toString())
            token_info_scopes.text = token.scopes
        } else {

            token_edit_access_token.text = null
            token_edit_refresh_token.text = null
            token_info_token_type.text = ""
            token_edit_expires_in.text = null
            token_edit_created_time.text = null
            token_info_scopes.text = ""
        }

        bttn_submit.setOnClickListener {

            val storManager = SharedPrefsStorageManager(this)
            val newToken = Token(
                accessToken = token_edit_access_token.text.toString(),
                refreshToken = token_edit_refresh_token.text.toString(),
                tokenType = token!!.tokenType,
                createdTime = token_edit_created_time.text?.toString()?.toLongOrNull() ?: 0L,
                expiresInSecs = token_edit_expires_in.text?.toString()?.toIntOrNull() ?: 0,
                scopes = token.scopes
            )

            storManager.saveToken(newToken, bearer!!.getAuthType())
        }
    }
}
