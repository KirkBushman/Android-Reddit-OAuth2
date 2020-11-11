package com.kirkbushman.auth

import com.kirkbushman.auth.http.RedditAuthClient
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.Token
import com.kirkbushman.auth.models.bearers.ScriptTokenBearer
import com.kirkbushman.auth.models.enums.AuthType
import com.kirkbushman.auth.models.bearers.TokenBearer
import com.kirkbushman.auth.models.creds.ScriptCredentials

// Script App Authentication
class ScriptAuth(

    private val client: RedditAuthClient,

    /**
     * Contains
     *
     * username, password:
     * Login info of the account.
     *
     * clientId:
     * Given by creating an app on reddit console, at https://www.reddit.com/prefs/apps,
     * Click the "create an app" button
     *
     * clientSecret:
     * Given by creating an app on reddit console, at https://www.reddit.com/prefs/apps,
     * Click the "create an app" button
     *
     */
    private val credentials: ScriptCredentials,

    /**
     * Interface instance that is used to persist token to memory.
     * Can be extended using the method you prefer, this lib provides a working
     * example with SharedPreferences
     */
    private val storManager: StorageManager

) : RedditAuth(
    client = client,
    credentials = credentials,
    storManager = storManager,
    authType = AuthType.SCRIPT
) {

    override fun renewToken(token: Token): Token? {

        return client.accessToken(
            clientId = credentials.clientId,
            clientSecret = credentials.clientSecret,
            username = credentials.username,
            password = credentials.password
        )
    }

    override fun revokeToken(token: Token): Boolean {

        return try {

            val res = client.revokeAccessToken(
                clientId = credentials.clientId,
                clientSecret = credentials.clientSecret,
                accessToken = token.accessToken
            )

            res != null
        } catch (ex: Exception) {
            ex.printStackTrace()

            false
        }
    }

    override fun fetchToken(): Token? {

        return client.accessToken(
            clientId = credentials.clientId,
            clientSecret = credentials.clientSecret,
            username = credentials.username,
            password = credentials.password
        )
    }

    fun authenticate(): TokenBearer? {

        try {

            val token = fetchToken()

            return if (token != null) {

                saveToken(token)

                ScriptTokenBearer(
                    storManager = storManager,
                    this
                )
            } else {
                null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()

            return null
        }
    }

    override fun retrieveSavedBearer(): TokenBearer? {

        if (!hasSavedBearer())
            return null

        return ScriptTokenBearer(
            storManager = storManager,
            scriptAuth = this
        )
    }
}
