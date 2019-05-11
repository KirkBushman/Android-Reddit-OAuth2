package com.kirkbushman.lib.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

/**
 * Token used to interact with the API.
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class Token(

    /**
     * The access token is used to make request to the API, to access data. (Basic Authentication)
     * It should last 1 hour and needs to be renewd.
     *
     * Authorization: bearer TOKEN (encoded in base64)
     */
    @Json(name = "access_token")
    val accessToken: String,

    /**
     * The refresh token is used to get new access tokens, when they expire.
     */
    @Json(name = "refresh_token")
    val refreshToken: String,

    /**
     * The string "bearer"
     */
    @Json(name = "token_type")
    val tokenType: String,

    /**
     * The duration of the accessToken in seconds, should be 3600
     */
    @Json(name = "expires_in")
    val expiresInSecs: Int,

    /**
     * Unix time value of the current date and time,
     * used to determine in the future the expirationTime adding expiresInSecs
     */
    val createdTime: Long = (System.currentTimeMillis() / 1000L),

    /**
     * Scopes this token will have while using this token,
     * specified at the beginning when making the initial request
     */
    @Json(name = "scope")
    val scopes: String

) : Parcelable, Serializable {

    @IgnoredOnParcel
    val expirationTime by lazy { createdTime + expiresInSecs }

    /**
     * The token should last an hour,
     * consider it safe for 55 mins, for precaution
     * 5 mins = 5 * 60 = 300
     */
    fun shouldRenew(): Boolean {
        val currentTimestamp = System.currentTimeMillis() / 1000L
        return (currentTimestamp + 300L) >= expirationTime
    }

    fun generateNewFrom(refreshToken: RefreshToken): Token {

        return Token(
            accessToken = refreshToken.accessToken,
            refreshToken = this.refreshToken,
            tokenType = refreshToken.tokenType,
            expiresInSecs = refreshToken.expiresInSecs,
            createdTime = refreshToken.createdTime,
            scopes = refreshToken.scopes
        )
    }

    override fun toString(): String {
        return "Token { accessToken: $accessToken, refreshToken: $refreshToken, tokenType: $tokenType, expiresInSecs: $expiresInSecs, createdTime: $createdTime, scope: $scopes }"
    }
}