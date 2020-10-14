package com.kirkbushman.sampleapp.local.tests

import com.kirkbushman.auth.RedditAuth
import com.kirkbushman.auth.managers.NoOpStorageManager
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.AuthType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

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
            .setScopes(arrayListOf())
            .setStorageManager(NoOpStorageManager())
            .setLogging(true)
            .build()

        assertTrue(auth.getAuthType() == AuthType.USERLESS)
    }

    @Test
    fun redditAuth_checkTypeScript() {

        val auth = RedditAuth.Builder()
            .setScriptAuthCredentials("", "", "", "")
            .setScopes(arrayListOf())
            .setStorageManager(NoOpStorageManager())
            .setLogging(true)
            .build()

        assertTrue(auth.getAuthType() == AuthType.SCRIPT)
    }

    @Test
    fun redditAuth_mockCheckBearer() {

        val storManager = Mockito.mock(StorageManager::class.java)

        Mockito.doReturn(true).`when`(storManager).hasToken()
        Mockito.doReturn(true).`when`(storManager).isAuthed()

        val auth = RedditAuth.Builder()
            .setUserlessCredentials("")
            .setScopes(arrayListOf())
            .setStorageManager(storManager)
            .setLogging(true)
            .build()

        assertTrue(auth.hasSavedBearer())
    }

    @Test
    fun redditAuth_mockCheckTokenType() {

        val storManager = Mockito.mock(StorageManager::class.java)

        Mockito.doReturn(AuthType.SCRIPT).`when`(storManager).authType()

        val auth = RedditAuth.Builder()
            .setUserlessCredentials("")
            .setScopes(arrayListOf())
            .setStorageManager(storManager)
            .setLogging(true)
            .build()

        assertEquals(AuthType.SCRIPT, auth.getSavedBearerType())
    }
}
