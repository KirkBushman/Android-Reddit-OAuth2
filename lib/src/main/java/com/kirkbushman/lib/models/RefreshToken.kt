package com.kirkbushman.lib.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RefreshToken(

    @Json(name = "access_token")
    val accessToken: String = "",

    @Json(name = "token_type")
    val tokenType: String,

    @Json(name = "expires_in")
    val expiresInSecs: Int,

    val createdTime: Long = (System.currentTimeMillis() / 1000L),

    @Json(name = "scope")
    val scopes: String

) {
    override fun toString(): String {
        return "RefreshToken { accessToken: $accessToken, tokenType: $tokenType, expiresInSecs: $expiresInSecs, createdTime: $createdTime, scopes: $scopes }"
    }
}