package com.kirkbushman.sampleapp.local.tests

import com.kirkbushman.auth.RedditAuth
import com.kirkbushman.auth.managers.NoOpStorageManager
import com.kirkbushman.auth.models.enums.AuthType
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthTests {

    @Test
    fun redditAuth_checkTypeApp() {

        val auth = RedditAuth.Builder()
            .setApplicationCredentials("", "")
            .setScopes(arrayListOf())
            .setStorageManager(NoOpStorageManager())
            .setLogging(true)
            .build()

        assertTrue(auth.getAuthType() == AuthType.INSTALLED_APP)
    }

    @Test
    fun redditAuth_checkTypeUserless() {

        val auth = RedditAuth.Builder()
            .setUserlessCredentials("")
            .setStorageManager(NoOpStorageManager())
            .setLogging(true)
            .build()

        assertTrue(auth.getAuthType() == AuthType.USERLESS)
    }

    @Test
    fun redditAuth_checkTypeScript() {

        val auth = RedditAuth.Builder()
            .setScriptAuthCredentials("", "", "", "")
            .setStorageManager(NoOpStorageManager())
            .setLogging(true)
            .build()

        assertTrue(auth.getAuthType() == AuthType.SCRIPT)
    }
}
