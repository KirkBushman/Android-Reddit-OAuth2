package com.kirkbushman.auth.models.bearers

import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.Token
import com.kirkbushman.auth.models.enums.AuthType

/**
 * Class that holds and manages the token.
 */
abstract class TokenBearer(

    /**
     * Used to save in memory the info, about token, auth, and basic info.
     */
    private val storManager: StorageManager,
) {

    private var isRevoked = false

    abstract fun renewToken(token: Token): Token?
    abstract fun revokeToken(token: Token): Boolean

    fun isAuthed(): Boolean {
        return storManager.isAuthed()
    }

    fun getAuthType(): AuthType {
        return storManager.authType()
    }

    fun getToken(): Token? {

        if (isRevoked()) {
            return null
        }

        if (shouldRenew()) {
            renewToken()
        }

        return storManager.getToken()
    }

    fun getAccessToken(): String? {

        if (isRevoked()) {
            return null
        }

        if (shouldRenew()) {
            renewToken()
        }

        return storManager.getToken()?.accessToken
    }

    fun getRefreshToken(): String? {

        return storManager.getToken()?.refreshToken
    }

    fun getAuthHeaderStr(): String? {

        if (isRevoked()) {
            return null
        }

        if (shouldRenew()) {
            renewToken()
        }

        return "Authorization: bearer ${getAccessToken()}"
    }

    fun getAuthHeader(): Map<String, String> {
        return hashMapOf("Authorization" to "bearer ".plus(getAccessToken()))
    }

    @Throws(IllegalStateException::class)
    fun renewToken() {

        if (isRevoked) {
            return
        }

        val token = storManager.getToken()
        if (token != null) {

            // If the request was successful replace the new token
            val newToken = renewToken(token)
            if (newToken != null) {

                // add back refresh toke if there is one
                val newToken2 = token.generateNewFrom(newToken)

                // and save it the store for the future
                storManager.saveToken(newToken2, storManager.authType())

                return
            }
        }

        throw IllegalStateException("Response was unsuccessful while renewing token!")
    }

    fun isRevoked(): Boolean {
        return isRevoked
    }

    private fun shouldRenew(): Boolean {
        val token = storManager.getToken()
        return token?.shouldRenew() ?: false
    }

    @Throws(IllegalStateException::class)
    fun revokeToken() {

        if (isRevoked) {
            return
        }

        val token = storManager.getToken()
        if (token != null) {

            val wasSuccessful = revokeToken(token)
            if (wasSuccessful) {

                // if successful set the token null, clear the one saven on store
                isRevoked = true

                storManager.clearAll()

                return
            }

            throw IllegalStateException("Response was unsuccessful while revoking access token!")
        }
    }
}
