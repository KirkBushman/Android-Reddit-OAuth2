package com.kirkbushman.sampleapp.local.tests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kirkbushman.auth.ScriptAuth
import com.kirkbushman.auth.http.RedditAuthClient
import com.kirkbushman.auth.managers.StorageManager
import com.kirkbushman.auth.models.Token
import com.kirkbushman.auth.models.bearers.ScriptTokenBearer
import com.kirkbushman.auth.models.creds.ScriptCredentials
import com.kirkbushman.auth.models.enums.AuthType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class ScriptAuthTests {

    companion object {

        private const val accessToken = "access_token"
        private val refreshToken = null
        private const val tokenType = "test_type"
        private const val expiresInSecs = 121212
        private const val createdTime = 12323423L
        private const val scopes = "scopes"

        private val testToken = Token(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = tokenType,
            expiresInSecs = expiresInSecs,
            createdTime = createdTime,
            scopes = scopes
        )
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var auth: ScriptAuth

    @Mock
    lateinit var client: RedditAuthClient
    @Mock
    lateinit var credentials: ScriptCredentials
    @Mock
    lateinit var storageManager: StorageManager

    @Before
    fun onPre() {
        auth = ScriptAuth(
            client,
            credentials,
            storageManager
        )
    }

    @Test
    fun authTest_checkType() {

        assertEquals(AuthType.SCRIPT, auth.getAuthType())
    }

    @Test
    fun authTest_renewToken() {

        Mockito.`when`(credentials.clientId).thenReturn("")
        Mockito.`when`(credentials.clientSecret).thenReturn("")
        Mockito.`when`(credentials.username).thenReturn("")
        Mockito.`when`(credentials.password).thenReturn("")

        Mockito
            .`when`(
                client.accessToken("", "", "", "")
            )
            .thenReturn(testToken)

        assertEquals(testToken, auth.renewToken(testToken))

        val newToken = auth.renewToken(testToken)
        assertEquals(accessToken, newToken!!.accessToken)
        assertEquals(refreshToken, newToken.refreshToken)
        assertEquals(tokenType, newToken.tokenType)
        assertEquals(expiresInSecs, newToken.expiresInSecs)
        assertEquals(createdTime, newToken.createdTime)
        assertEquals(scopes, newToken.scopes)
    }

    @Test
    fun authTest_renewTokenNull() {

        Mockito.`when`(credentials.clientId).thenReturn("")
        Mockito.`when`(credentials.clientSecret).thenReturn("")
        Mockito.`when`(credentials.username).thenReturn("")
        Mockito.`when`(credentials.password).thenReturn("")

        Mockito
            .`when`(
                client.accessToken("", "", "", "")
            )
            .thenReturn(null)

        assertEquals(null, auth.renewToken(testToken))
    }

    @Test
    fun authTest_revokeTokenError() {

        Mockito.`when`(credentials.clientId).thenReturn("")
        Mockito.`when`(credentials.clientSecret).thenReturn("")

        Mockito
            .`when`(
                client.revokeAccessToken("", "", testToken.accessToken)
            )
            .thenThrow(IllegalStateException("test exception"))

        assertEquals(false, auth.revokeToken(testToken))
    }

    @Test
    fun authTest_revokeTokenNull() {

        Mockito.`when`(credentials.clientId).thenReturn("")
        Mockito.`when`(credentials.clientSecret).thenReturn("")

        Mockito
            .`when`(
                client.revokeAccessToken("", "", testToken.accessToken)
            )
            .thenReturn(null)

        assertEquals(false, auth.revokeToken(testToken))
    }

    @Test
    fun authTest_revokeTokenRight() {

        Mockito.`when`(credentials.clientId).thenReturn("")
        Mockito.`when`(credentials.clientSecret).thenReturn("")

        val response = Mockito.mock(ResponseBody::class.java)

        Mockito
            .`when`(
                client.revokeAccessToken("", "", testToken.accessToken)
            )
            .thenReturn(response)

        assertEquals(true, auth.revokeToken(testToken))
    }

    @Test
    fun authTest_authenticateNullToken() {

        Mockito.`when`(credentials.clientId).thenReturn("")
        Mockito.`when`(credentials.clientSecret).thenReturn("")
        Mockito.`when`(credentials.username).thenReturn("")
        Mockito.`when`(credentials.password).thenReturn("")

        Mockito
            .`when`(
                client.accessToken("", "", "", "")
            )
            .thenReturn(null)

        val bearer = auth.authenticate()

        assertEquals(null, bearer)
    }

    @Test
    fun authTest_authenticateError() {

        Mockito.`when`(credentials.clientId).thenReturn("")
        Mockito.`when`(credentials.clientSecret).thenReturn("")
        Mockito.`when`(credentials.username).thenReturn("")
        Mockito.`when`(credentials.password).thenReturn("")

        Mockito
            .`when`(
                client.accessToken("", "", "", "")
            )
            .thenThrow(IllegalStateException("test exception"))

        val bearer = auth.authenticate()

        assertEquals(null, bearer)
    }

    @Test
    fun authTest_authenticateRight() {

        Mockito.`when`(credentials.clientId).thenReturn("")
        Mockito.`when`(credentials.clientSecret).thenReturn("")
        Mockito.`when`(credentials.username).thenReturn("")
        Mockito.`when`(credentials.password).thenReturn("")

        Mockito
            .`when`(
                client.accessToken("", "", "", "")
            )
            .thenReturn(testToken)

        val bearer = auth.authenticate()

        assertTrue(bearer != null)
        assertTrue(bearer is ScriptTokenBearer)
    }

    @Test
    fun authTest_hasSavedBearerWrongType() {

        Mockito.`when`(storageManager.isAuthed()).thenReturn(true)
        Mockito.`when`(storageManager.hasToken()).thenReturn(true)
        Mockito.`when`(storageManager.authType()).thenReturn(AuthType.USERLESS)

        assertFalse(auth.hasSavedBearer())
    }

    @Test
    fun authTest_hasSavedBearerEmpty() {

        Mockito.`when`(storageManager.isAuthed()).thenReturn(false)
        Mockito.`when`(storageManager.hasToken()).thenReturn(false)
        Mockito.`when`(storageManager.authType()).thenReturn(AuthType.SCRIPT)

        assertFalse(auth.hasSavedBearer())
    }

    @Test
    fun authTest_hasSavedBearerRight() {

        Mockito.`when`(storageManager.isAuthed()).thenReturn(true)
        Mockito.`when`(storageManager.hasToken()).thenReturn(true)
        Mockito.`when`(storageManager.authType()).thenReturn(AuthType.SCRIPT)

        assertTrue(auth.hasSavedBearer())
    }
}
