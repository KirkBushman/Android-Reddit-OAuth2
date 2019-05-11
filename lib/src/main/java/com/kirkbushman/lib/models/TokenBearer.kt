package com.kirkbushman.lib.models

import com.kirkbushman.lib.managers.RedditAuthManager
import com.kirkbushman.lib.managers.StorageManager
import com.kirkbushman.lib.utils.toHeaderString

/**
 * Class that holds and manages the token.
 */
class TokenBearer(

    /**
     * Used to save in memory the info, about token, auth, and basic info.
     */
    private val storManager: StorageManager,

    /**
     * The current token that is being used, should be null only if revoked.
     */
    private var token: Token?,

    /**
     * ClientId and Redirect Url
     */
    private val basicInfo: BasicInfo

) {

    private var isRevoked = false

    init {

        if (token != null) {
            storManager.saveToken(token!!)
        }

        if (token == null) {
            isRevoked = true
            storManager.deleteToken()
        }
    }

    fun getRawAccessToken(): String? {

        if (isRevoked()) {
            return null
        }

        if (shouldRenew()) {
            renewToken()
        }

        return token!!.accessToken
    }

    fun getTokenHttpHeader(): String? {

        if (isRevoked()) {
            return null
        }

        return "Authorization: bearer ${getRawAccessToken()}"
    }

    fun revokeToken() {

        if (isRevoked) {
            return
        }

        val req = RedditAuthManager.api.revoke(
            header = "${basicInfo.clientId}:".toHeaderString(),

            token = token!!.accessToken,
            tokenTypeHint = "access_token"
        )

        val res = req.execute()
        if (res.isSuccessful) {

            // if successful set the token null, clear the one saven on store
            token = null
            isRevoked = true

            storManager.deleteToken()

            return
        }

        throw IllegalStateException("Response was unsuccessful while revoking access token!")
    }

    fun isRevoked(): Boolean {
        return isRevoked
    }

    private fun shouldRenew(): Boolean {
        return token?.shouldRenew() ?: false
    }

    fun renewToken() {

        if (isRevoked) {
            return
        }

        val req = RedditAuthManager.api.renewToken(

            header = "${basicInfo.clientId}:".toHeaderString(),
            refreshToken = token!!.refreshToken
        )

        val res = req.execute()
        if (res.isSuccessful) {

            val newRefreshToken = (res.body()
                ?: IllegalStateException("Response is null!")) as RefreshToken

            // If the request was successful replace the new token
            token = token!!.generateNewFrom(newRefreshToken)

            // and save it the store for the future
            storManager.saveToken(token!!)

            return
        }

        IllegalStateException("Response was unsuccessful while renewing token!")
    }

    override fun toString(): String {
        return "TokenBearer { token: $token, isRevoked: $isRevoked }"
    }
}