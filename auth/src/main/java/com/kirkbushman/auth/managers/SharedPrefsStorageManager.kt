package com.kirkbushman.auth.managers

import android.content.Context
import androidx.core.content.edit
import com.kirkbushman.auth.models.enums.AuthType
import com.kirkbushman.auth.models.Token

/**
 * Implementation of StorageManager using SharedPreferences
 */
class SharedPrefsStorageManager(context: Context) : StorageManager {

    companion object {

        internal const val INTERNAL_SHARED_PREFS = "internal_shared_prefs_default"

        private const val IS_AUTHED = "android_reddit_oauth2_is_authed_first_time"

        private const val AUTH_TYPE = "android_reddit_oauth2_authentication_type"

        private const val LAST_ACCESS_TOKEN = "android_reddit_oauth2_current_access_token"
        private const val LAST_REFRESH_TOKEN = "android_reddit_oauth2_current_refresh_token"
        private const val LAST_TOKEN_TYPE = "android_reddit_oauth2_current_token_type"
        private const val LAST_EXPIRES_IN = "android_reddit_oauth2_current_expires_in"
        private const val LAST_CREATED_TIME = "android_reddit_oauth2_current_created_time"
        private const val LAST_SCOPES = "android_reddit_oauth2_current_scopes"
    }

    private val prefs by lazy { context.getSharedPreferences(INTERNAL_SHARED_PREFS, Context.MODE_PRIVATE) }

    override fun isAuthed(): Boolean {
        return prefs.getBoolean(IS_AUTHED, false)
    }

    override fun authType(): AuthType {
        return AuthType.valueOf(prefs.getString(AUTH_TYPE, "NONE") ?: "NONE")
    }

    override fun hasToken(): Boolean {
        return prefs.contains(LAST_ACCESS_TOKEN) &&
            prefs.contains(LAST_TOKEN_TYPE) &&
            prefs.contains(LAST_EXPIRES_IN) &&
            prefs.contains(LAST_CREATED_TIME) &&
            prefs.contains(LAST_SCOPES)
    }

    override fun getToken(): Token {
        if (!hasToken()) {
            throw IllegalStateException("Token not found in store! did you ever saved one?")
        }

        val accessToken = prefs.getString(LAST_ACCESS_TOKEN, "") as String
        val refreshToken = prefs.getString(LAST_REFRESH_TOKEN, null)
        val tokenType = prefs.getString(LAST_TOKEN_TYPE, "") as String
        val expiresInSecs = prefs.getInt(LAST_EXPIRES_IN, 0)
        val createdTime = prefs.getLong(LAST_CREATED_TIME, 0L)
        val scopes = prefs.getString(LAST_SCOPES, "") as String

        return Token(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = tokenType,
            expiresInSecs = expiresInSecs,
            createdTime = createdTime,
            scopes = scopes
        )
    }

    override fun saveToken(token: Token, authType: AuthType) {

        prefs.edit {

            if (!prefs.contains(IS_AUTHED) || !prefs.getBoolean(IS_AUTHED, false)) {
                putBoolean(IS_AUTHED, true)
            }

            putString(AUTH_TYPE, authType.toString())

            putString(LAST_ACCESS_TOKEN, token.accessToken)
            putString(LAST_REFRESH_TOKEN, token.refreshToken)
            putString(LAST_TOKEN_TYPE, token.tokenType)
            putInt(LAST_EXPIRES_IN, token.expiresInSecs)
            putLong(LAST_CREATED_TIME, token.createdTime)
            putString(LAST_SCOPES, token.scopes)
        }
    }

    override fun clearAll() {
        prefs.edit {

            if (prefs.contains(IS_AUTHED))
                this.remove(IS_AUTHED)

            if (prefs.contains(AUTH_TYPE))
                this.remove(AUTH_TYPE)

            if (prefs.contains(LAST_ACCESS_TOKEN))
                this.remove(LAST_ACCESS_TOKEN)
            if (prefs.contains(LAST_REFRESH_TOKEN))
                this.remove(LAST_REFRESH_TOKEN)
            if (prefs.contains(LAST_TOKEN_TYPE))
                this.remove(LAST_TOKEN_TYPE)
            if (prefs.contains(LAST_EXPIRES_IN))
                this.remove(LAST_EXPIRES_IN)
            if (prefs.contains(LAST_CREATED_TIME))
                this.remove(LAST_CREATED_TIME)
            if (prefs.contains(LAST_SCOPES))
                this.remove(LAST_SCOPES)
        }
    }
}
