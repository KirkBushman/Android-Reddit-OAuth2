package com.kirkbushman.auth.http

import com.kirkbushman.auth.models.RefreshToken
import com.kirkbushman.auth.models.ScopesEnvelope
import com.kirkbushman.auth.models.Token
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface RedditService {

    @GET
    fun getScopes(
        @Url url: String = "https://www.reddit.com/api/v1/scopes"
    ): Call<ScopesEnvelope>

    @POST
    fun getAccessToken(
        @Url url: String = "https://www.reddit.com/api/v1/access_token",
        @HeaderMap header: HashMap<String, String>,

        @Query("grant_type") grantType: String,
        @Query("code") code: String? = null,
        @Query("redirect_uri") redirectUrl: String? = null,
        @Query("username") username: String? = null,
        @Query("password") password: String? = null
    ): Call<Token>

    @POST
    fun renewToken(
        @Url url: String = "https://www.reddit.com/api/v1/access_token",
        @HeaderMap header: HashMap<String, String>,

        @Query("grant_type") grantType: String = "refresh_token",
        @Query("refresh_token") refreshToken: String
    ): Call<RefreshToken>

    @POST
    fun revoke(
        @Url url: String = "https://www.reddit.com/api/v1/revoke_token",
        @HeaderMap header: HashMap<String, String>,

        @Query("token") token: String,
        @Query("token_type_hint") tokenTypeHint: String
    ): Call<Any>
}
