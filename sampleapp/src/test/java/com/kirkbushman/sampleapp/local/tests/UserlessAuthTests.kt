package com.kirkbushman.sampleapp.local.tests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kirkbushman.auth.UserlessAuth
import com.kirkbushman.auth.http.RedditAuthClient
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.creds.UserlessCredentials
import com.kirkbushman.auth.models.enums.AuthType
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class UserlessAuthTests {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var auth: UserlessAuth

    @Mock
    lateinit var client: RedditAuthClient
    @Mock
    lateinit var credentials: UserlessCredentials
    @Mock
    lateinit var storageManager: StorageManager

    @Before
    fun onPre() {
        auth = UserlessAuth(
            client,
            credentials,
            storageManager
        )
    }

    @Test
    fun authTest_checkType() {

        Assert.assertEquals(AuthType.USERLESS, auth.getAuthType())
    }

    @Test
    fun authTest_hasSavedBearerWrongType() {

        Mockito.`when`(storageManager.isAuthed()).thenReturn(true)
        Mockito.`when`(storageManager.hasToken()).thenReturn(true)
        Mockito.`when`(storageManager.authType()).thenReturn(AuthType.SCRIPT)

        Assert.assertFalse(auth.hasSavedBearer())
    }

    @Test
    fun authTest_hasSavedBearerEmpty() {

        Mockito.`when`(storageManager.isAuthed()).thenReturn(false)
        Mockito.`when`(storageManager.hasToken()).thenReturn(false)
        Mockito.`when`(storageManager.authType()).thenReturn(AuthType.USERLESS)

        Assert.assertFalse(auth.hasSavedBearer())
    }

    @Test
    fun authTest_hasSavedBearerRight() {

        Mockito.`when`(storageManager.isAuthed()).thenReturn(true)
        Mockito.`when`(storageManager.hasToken()).thenReturn(true)
        Mockito.`when`(storageManager.authType()).thenReturn(AuthType.USERLESS)

        Assert.assertTrue(auth.hasSavedBearer())
    }
}
