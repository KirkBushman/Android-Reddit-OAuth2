package com.kirkbushman.auth.http

import com.kirkbushman.auth.models.RefreshToken
import com.kirkbushman.auth.models.ScopesEnvelope
import com.kirkbushman.auth.models.Token
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Query

interface RedditService {

    @GET("api/v1/scopes")
    fun getScopes(): Call<ScopesEnvelope>

    @POST("api/v1/access_token")
    fun getAccessToken(
        @HeaderMap header: HashMap<String, String>,

        @Query("grant_type") grantType: String = "authorization_code",
        @Query("code") code: String,
        @Query("redirect_uri") redirectUrl: String
    ): Call<Token>

    @POST("api/v1/access_token")
    fun renewToken(
        @HeaderMap header: HashMap<String, String>,

        @Query("grant_type") grantType: String = "refresh_token",
        @Query("refresh_token") refreshToken: String
    ): Call<RefreshToken>

    @POST("api/v1/revoke_token")
    fun revoke(
        @HeaderMap header: HashMap<String, String>,

        @Query("token") token: String,
        @Query("token_type_hint") tokenTypeHint: String
    ): Call<Any>
}