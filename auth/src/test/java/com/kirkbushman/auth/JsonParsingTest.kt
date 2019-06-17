package com.kirkbushman.auth

import com.kirkbushman.auth.http.RedditService
import com.kirkbushman.auth.managers.RedditAuthManager
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class JsonParsingTest {

    @Test
    fun testForScopes() {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.reddit.com")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val api = retrofit.create(RedditService::class.java)
        val response = api.getScopes().execute()

        assert(response.isSuccessful)
        if (response.isSuccessful) {

            val scopes = response.body()
            assert(scopes != null)
            if (scopes != null) {

                assert(scopes.toScopesArray().isNotEmpty())
            }
        }
    }

    @Test
    fun testForScopesFromAuthClient() {

        try {
            val scopes = RedditAuthManager.getScopes()
            assert(scopes != null)
            if (scopes != null) {

                assert(scopes.toScopesArray().isNotEmpty())
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            assert(false)
        }
    }
}