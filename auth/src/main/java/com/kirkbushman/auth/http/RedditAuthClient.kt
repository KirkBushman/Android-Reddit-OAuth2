package com.kirkbushman.auth.http

import com.kirkbushman.auth.errors.OAuth2Exception
import com.kirkbushman.auth.models.ScopesEnvelope
import com.kirkbushman.auth.models.Token
import com.kirkbushman.auth.utils.toHeaderString
import okhttp3.ResponseBody

class RedditAuthClient(private val api: RedditAuthService) {

    fun getScopes(): ScopesEnvelope? {

        val req = api.getScopes()
        val res = req.execute()

        if (res.isSuccessful) {
            return res.body()
        } else {
            throw OAuth2Exception(res.errorBody().toString())
        }
    }

    fun accessToken(
        clientId: String,
        redirectUrl: String,
        authCode: String
    ): Token? {

        val req = api.getAccessToken(
            header = "$clientId:".toHeaderString(),
            grantType = "authorization_code",
            code = authCode,
            redirectUrl = redirectUrl
        )

        val res = req.execute()
        if (res.isSuccessful) {
            return res.body()
        } else {
            throw OAuth2Exception(res.errorBody().toString())
        }
    }

    fun accessToken(
        clientId: String,
        clientSecret: String,
        username: String,
        password: String
    ): Token? {

        val req = api.getAccessToken(
            header = "$clientId:$clientSecret".toHeaderString(),
            grantType = "password",
            username = username,
            password = password
        )

        val res = req.execute()
        if (res.isSuccessful) {
            return res.body()
        } else {
            throw OAuth2Exception(res.errorBody().toString())
        }
    }

    fun accessToken(
        clientId: String,
        deviceId: String
    ): Token? {

        val req = api.getAccessToken(
            header = "$clientId:".toHeaderString(),
            grantType = "https://oauth.reddit.com/grants/installed_client",
            deviceId = deviceId
        )

        val res = req.execute()
        if (res.isSuccessful) {
            return res.body()
        } else {
            throw OAuth2Exception(res.errorBody().toString())
        }
    }

    fun renewRefreshToken(
        clientId: String,
        refreshToken: String
    ): Token? {

        val req = api.renewToken(
            header = "$clientId:".toHeaderString(),
            refreshToken = refreshToken
        )

        val res = req.execute()
        if (res.isSuccessful) {
            return res.body()
        } else {
            throw OAuth2Exception(res.errorBody().toString())
        }
    }

    fun revokeRefreshToken(
        clientId: String,
        refreshToken: String
    ): ResponseBody? {

        val req = api.revoke(
            header = "$clientId:".toHeaderString(),
            token = refreshToken,
            tokenTypeHint = "refresh_token"
        )

        val res = req.execute()
        if (res.isSuccessful) {
            return res.body()
        } else {
            throw OAuth2Exception(res.errorBody().toString())
        }
    }

    fun revokeAccessToken(
        clientId: String,
        accessToken: String
    ): ResponseBody? {

        val req = api.revoke(
            header = "$clientId:".toHeaderString(),
            token = accessToken,
            tokenTypeHint = "access_token"
        )

        val res = req.execute()
        if (res.isSuccessful) {
            return res.body()
        } else {
            throw OAuth2Exception(res.errorBody().toString())
        }
    }

    fun revokeAccessToken(
        clientId: String,
        clientSecret: String,
        accessToken: String
    ): ResponseBody? {

        val req = api.revoke(
            header = "$clientId:$clientSecret".toHeaderString(),
            token = accessToken,
            tokenTypeHint = "access_token"
        )

        val res = req.execute()
        if (res.isSuccessful) {
            return res.body()
        } else {
            throw OAuth2Exception(res.errorBody().toString())
        }
    }
}
