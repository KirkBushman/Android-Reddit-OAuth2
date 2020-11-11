package com.kirkbushman.auth

import com.kirkbushman.auth.http.RedditAuthClient
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.Token
import com.kirkbushman.auth.models.enums.AuthType
import com.kirkbushman.auth.models.bearers.TokenBearer
import com.kirkbushman.auth.models.bearers.UserlessTokenBearer
import com.kirkbushman.auth.models.creds.UserlessCredentials

// App Authentication without user-context
class UserlessAuth(

    private val client: RedditAuthClient,

    /**
     * Contains
     *
     * clientId:
     * Given by creating an app on reddit console, at https://www.reddit.com/prefs/apps,
     * Click the "create an app" button,
     *
     * deviceId:
     * A unique string that identified a device, needed since there is no user reference.
     * If this is null, it will be generated for you.
     */
    private val credentials: UserlessCredentials,

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
    authType = AuthType.USERLESS
) {

    override fun renewToken(token: Token): Token? {

        return client.accessToken(
            clientId = credentials.clientId,
            deviceId = credentials.deviceId
        )
    }

    override fun revokeToken(token: Token): Boolean {

        return try {

            val res = client.revokeAccessToken(
                clientId = credentials.clientId,
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
            deviceId = credentials.deviceId
        )
    }

    fun authenticate(): TokenBearer? {

        try {

            val token = fetchToken()

            return if (token != null) {

                saveToken(token)

                UserlessTokenBearer(
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

        return UserlessTokenBearer(
            storManager = storManager,
            userlessAuth = this
        )
    }
}
